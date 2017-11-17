package ru.tpgeovk.back.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.xpath.internal.operations.Mod;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.places.PlaceFull;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.queries.groups.GroupField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tpgeovk.back.contexts.GsonContext;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.*;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.scripts.VkScripts;
import ru.tpgeovk.back.text.TextProcessor;
import ru.tpgeovk.back.util.ModelUtil;

import java.util.*;
import java.util.stream.Collectors;

import static ru.tpgeovk.back.scripts.VkScripts.PLACE_CHECKINS_USERS;

@Service
public class RecommendationService {

    private final VkApiClient vk;

    private final Gson gson;

    private final VkProxyService vkProxyService;

    @Autowired
    public RecommendationService(VkProxyService vkProxyService) {
        vk = VkContext.getVkApiClient();
        gson = GsonContext.createGson();
        this.vkProxyService = vkProxyService;
    }

    public Map<Integer, List<Integer>> getSimilarUsers(UserActor actor, List<Integer> userIds) throws VkException {
        if ((userIds == null) || (userIds.isEmpty())) {
            new HashMap<>();
        }
        Integer maxCommon = 0;
        Map<Integer, List<Integer>> commonGroups = new HashMap<>();
        for (Integer id : userIds) {
            commonGroups.put(id, new ArrayList<>());
        }

        int start = 0;
        int end = 500;
        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }
            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 500;
            end = end + 500;

            Integer groupsOffset = 0;
            JsonElement response = null;
            String script;
            while (groupsOffset < 200) {
                try {
                    script = String.format(VkScripts.GROUP_MEMBERS, actor.getId(), groupsOffset, currentIds.toString());
                    response = vk.execute().code(actor, script).execute();
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        e.printStackTrace();
                        throw new VkException(e.getMessage(), e);
                    }
                }

                if (response.getAsJsonArray().size() == 0) {
                    break;
                }
                groupsOffset = groupsOffset + 24;

                JsonArray groupsArray = response.getAsJsonArray();
                for (JsonElement group : groupsArray) {
                    JsonObject groupObject = group.getAsJsonObject();
                    Integer groupId = groupObject.getAsJsonPrimitive("groupId").getAsInt();
                    JsonElement membersElement = groupObject.get("members");

                    /** Проверка для забаненных сообществ */
                    if (membersElement.isJsonArray()) {
                        for (JsonElement member : membersElement.getAsJsonArray()) {
                            JsonObject memberObject = member.getAsJsonObject();
                            Integer userId = memberObject.getAsJsonPrimitive("user_id")
                                    .getAsInt();
                            if (!userId.equals(actor.getId())) {
                                List<Integer> groupList = commonGroups.get(userId);
                                if (memberObject.getAsJsonPrimitive("member").getAsInt() == 1) {
                                    groupList.add(groupId);
                                    if (groupList.size() > maxCommon) {
                                        maxCommon = groupList.size();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        final Integer threshold = maxCommon / 3;
        return commonGroups.entrySet().stream()
                .filter(a -> (a.getValue().size() != 0) && (a.getValue().size() >= threshold) &&
                        (!a.getKey().equals(actor.getId())))
                .sorted((a, b) -> (a.getValue().size() - b.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, HashMap::new));
    }

    public List<UserInfo> recommendFriends(UserActor actor) throws VkException {
        List<CheckinInfo> checkins = vkProxyService.getAllUserCheckins(actor);
        List<Integer> users = vkProxyService.getUsersFromCheckins(actor, checkins);

        List<UserInfo> byMutualFriends = getUsersWithMutualFriends(actor, users);
        List<UserInfo> byInterests = getUsersWithCommonInterests(actor, users);

        byMutualFriends.addAll(byInterests);
        return byMutualFriends.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public List<UserInfo> getUsersWithCommonInterests(UserActor actor, List<Integer> userIds) throws VkException {
        List<UserFeatures> usersFeatures = vkProxyService.getUsersFeatures(actor, userIds);
        UserFeatures actorFeatures = vkProxyService.getActorFeatures(actor);

        Map<Integer, Integer> usersRatings = new HashMap<>();
        Integer maxRating = 0;
        for (UserFeatures userFeatures : usersFeatures) {
            Integer commonGroupsCount = ModelUtil.countCommonGroups(actorFeatures.getGroups(),
                    userFeatures.getGroups());
            Integer sameAge = ModelUtil.isAgeSimilar(actorFeatures.getAge(), userFeatures.getAge()) ? 2 : 1;

            Integer rating = sameAge * commonGroupsCount;
            if (rating > maxRating) {
                maxRating = rating;
            }
            usersRatings.put(userFeatures.getUserId(), rating);
        }

        Integer threshold = maxRating / 3;
        List<Integer> users = usersRatings.entrySet().stream()
                .filter(a -> a.getValue() >= threshold)
                .map(a -> a.getKey())
                .collect(Collectors.toList());

        return vkProxyService.getUsers(actor, users, "COMMON_INTERESTS");
    }

    public List<UserInfo> getUsersWithMutualFriends(UserActor actor, List<Integer> userIds) throws VkException {
        Map<Integer, Integer> withMutualFriends = new HashMap<>();

        boolean ok;
        JsonElement response = null;
        String script;

        int start = 0;
        int end = 100;
        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }

            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 100;
            end = end + 100;

            ok = false;
            script = String.format(VkScripts.GET_MUTUAL_FRIENDS, currentIds.toString());
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        e.printStackTrace();
                        throw new VkException(e.getMessage(), e);
                    }
                }
            }

            JsonArray mutualFriendsField = response.getAsJsonArray();
            for (JsonElement user : mutualFriendsField) {
                JsonObject userObject =user.getAsJsonObject();
                Integer userId = userObject.getAsJsonPrimitive("id").getAsInt();
                Integer mutualFriendsCount = userObject.getAsJsonPrimitive("common_count").getAsInt();
                if (!mutualFriendsCount.equals(0)) {
                    withMutualFriends.put(userId, mutualFriendsCount);
                }
            }
        }

        List<UserInfo> usersInfo = vkProxyService.getUsers(actor, new ArrayList<>(withMutualFriends.keySet()));
        return usersInfo.stream()
                .map(a -> {
                    a.setReason(withMutualFriends.get(a.getId()).toString() + " общих друзей");
                    return a;
                })
                .collect(Collectors.toList());
    }

    public List<GroupInfo> recommendGroups(UserActor actor) throws VkException {
        List<CheckinInfo> checkins = vkProxyService.getAllUserCheckins(actor);
        List<GroupInfo> byPlaces = recommendGroupsByCheckinsPlaces(actor, checkins);
        List<GroupInfo> byUsers = recommendGroupsByCheckinsUsers(actor, checkins);

        byPlaces.addAll(byUsers);
        Collections.shuffle(byPlaces);

        return byPlaces;
    }

    public List<GroupInfo> recommendGroupsByCheckinsPlaces(UserActor actor, List<CheckinInfo> checkins)
            throws VkException {
        if ((checkins == null) || (checkins.isEmpty())) {
            return new ArrayList<>();
        }
        List<GroupInfo> result = new ArrayList<>();

        List<String> visitedGroupsIds = checkins.stream()
                .filter(a -> (a.getPlace() != null) && (a.getPlace().getGroupId() != null) &&
                        (!a.getPlace().getGroupId().equals(0)))
                .map(a -> a.getPlace().getGroupId().toString())
                .collect(Collectors.toList());

        if (visitedGroupsIds.size() != 0) {
            boolean requestOk = false;
            List<GroupFull> response = null;
            while (!requestOk) {
                try {
                    response = vk.groups().getById(actor)
                            .groupIds(visitedGroupsIds)
                            .fields(GroupField.MEMBERS_COUNT, GroupField.DESCRIPTION)
                            .execute();
                    requestOk = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }
            result.addAll(response.stream().map(a -> GroupInfo.fromGroupFull(a)).collect(Collectors.toList()));
        }

        List<String> allTitles = checkins.stream()
                .filter(a -> (a.getPlace() != null) && (!StringUtils.isEmpty(a.getPlace().getTitle())))
                .map(a -> TextProcessor.filterText(a.getPlace().getTitle()))
                .collect(Collectors.toList());
        if (allTitles.size() == 0) {
            return result;
        }

        JsonElement response = null;
        String script;
        List<GroupFull> groupFullResult = new ArrayList<>();
        loop1:
        for (String title : allTitles) {
            if (StringUtils.isEmpty(title)) {
                continue loop1;
            }

            script = String.format(VkScripts.GROUPS_SEARCH, title);
            boolean ok = false;
            loop2:
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue loop2;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }
            if (response.getAsJsonArray().size() == 0) {
                continue loop1;
            }

            List<GroupFull> groups = gson.fromJson(response, new TypeToken<List<GroupFull>>() {
            }.getType());
            groupFullResult.addAll(groups);
        }

        groupFullResult = groupFullResult.stream()
                .distinct()
                .collect(Collectors.toList());
        result.addAll(groupFullResult.stream().map(a -> GroupInfo.fromGroupFull(a)).collect(Collectors.toList()));

        return result.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public List<GroupInfo> recommendGroupsByCheckinsUsers(UserActor actor, List<CheckinInfo> userCheckins)
            throws VkException {

        List<Integer> userIds = vkProxyService.getUsersFromCheckins(actor, userCheckins);

        Map<String, Integer> groupRatings = new HashMap<>();

        JsonElement response = null;
        String script = null;
        boolean ok;

        int start = 0;
        int end = 25;

        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }

            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 25;
            end = end + 25;

            script = String.format(VkScripts.GET_USERS_GROUPS, currentIds.toString());
            ok = false;
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }

            if (response.isJsonArray()) {
                for (JsonElement groupElement : response.getAsJsonArray()) {
                    String group = groupElement.getAsString();
                    Integer groupRating = groupRatings.get(group);
                    groupRating = groupRating == null ? 0 : groupRating + 1;
                    groupRatings.put(group, groupRating);
                }
            } else {
                JsonElement responseElement = response.getAsJsonObject().getAsJsonArray("response");
                String group = responseElement.getAsString();
                Integer groupRating = groupRatings.get(group);
                groupRating = groupRating == null ? 0 : groupRating + 1;
                groupRatings.put(group, groupRating);
            }
        }

        Integer maxRating = groupRatings.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .getAsInt();

        if (maxRating.equals(0)) {
            return new ArrayList<>();
        }

        Integer threshold = maxRating / 10;

        List<String> groupIds = groupRatings.entrySet().stream()
                .filter(a -> a.getValue() > threshold)
                .map(a -> a.getKey())
                .collect(Collectors.toList());

        ok = false;
        List<GroupFull> groupFullList = null;
        while (!ok) {
            try {
                groupFullList = vk.groups().getById(actor)
                        .groupIds(groupIds)
                        .fields(GroupField.MEMBERS_COUNT, GroupField.DESCRIPTION)
                        .execute();
                ok = true;
            } catch (ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(50);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                e.printStackTrace();
                throw new VkException(e.getMessage(), e);
            }
        }

        return groupFullList.stream()
                .map(a -> GroupInfo.fromGroupFull(a))
                .filter(a -> (a.getMembersCount() != null) && (a.getMembersCount() < 400000))
                .collect(Collectors.toList());
    }

    public Double compareGroups(UserActor actor, GroupFull group1, GroupFull group2) throws VkException {
        String text1 = group1.getName() + " " + group1.getDescription();
        String text2 = group2.getName() + " " + group2.getDescription();

        String script = "return API.wall.get({\"owner_id\":" + group1.getId().toString() + ", \"filter\":\"owner\"," +
                "\"count\":10}).items@.text;";
        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e);
        }
        String[] posts1 = gson.fromJson(response, String[].class);

        script = "return API.wall.get({\"owner_id\":" + group2.getId().toString() + ", \"filter\":\"owner\"," +
                "\"count\":10}).items@.text;";
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e);
        }
        String[] posts2 = gson.fromJson(response, String[].class);

        for (String str : posts1) {
            text1 = text1 + " " + str;
        }
        for (String str : posts2) {
            text2 = text2 + " " + str;
        }

        return (double) TextProcessor.compareTexts(text1, text2);
    }


}
