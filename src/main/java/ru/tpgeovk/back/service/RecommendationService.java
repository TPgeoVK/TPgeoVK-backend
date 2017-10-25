package ru.tpgeovk.back.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.places.Checkin;
import com.vk.api.sdk.objects.places.PlaceFull;
import com.vk.api.sdk.objects.places.responses.GetCheckinsResponse;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.queries.groups.GroupField;
import com.vk.api.sdk.queries.groups.GroupsGetFilter;
import com.vk.api.sdk.queries.groups.GroupsGetMembersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.GoogleException;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.*;
import ru.tpgeovk.back.text.TextProcessor;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final String SCRIPT_EVENTS = "var events = API.groups.search({\"q\":\"*\",\"cityId\":%d,\"count\":25,\"type\":\"event\",\"future\":true,});\n" +
            "var eventIds = events.items@.id;\nreturn API.groups.getById({\"group_ids\":eventIds,\"fields\":\"description,members_count,place\"});\n";

    private static final String PLACE_CHECKINS_USERS = "var placeId = %d;\n" +
            "var checkins = API.places.getCheckins({\"place\":placeId});\n" +
            "var users = checkins.items@.user_id;\n" +
            "var total = checkins.count;\n" +
            "if (total <= 20) { return users; }\n" +
            "var offset = 20;\n" +
            "while ((offset < (total-20)) && (offset <= 400)) {\n" +
            "offset = offset + 20;\n" +
            "users = users + API.places.getCheckins({\"place\":placeId,\"offset\":offset}).items@.user_id;\n" +
            "}\n" +
            "return users;";

    private static final String COORD_CHECKINS_USERS = "var lat = %f;\nvar lon = %f;\n" +
            "var checkins = API.places.getCheckins({\"latitude\":lat,\"longitude\":lon});\n" +
            "var users = checkins.items@.user_id;\n" +
            "var total = checkins.count;\n" +
            "if (total <= 20) { return users; }\n" +
            "var offset = 20;\n" +
            "while ((offset < (total-20)) && (offset <= 400)) {\n" +
            "offset = offset + 20;\n" +
            "users = users + API.places.getCheckins({\"latitude\":lat,\"longitude\":lon,\"offset\":offset}).items@.user_id;\n" +
            "}\n" +
            "return users;";

    private static final String GROUP_MEMBERS = "var userId = %d;\nvar groupsOffset = %d;\nvar users = %s;\n" +
            "var groups = API.groups.get({\"user_id\":userId,\"offset\":groupsOffset,\"count\":24}).items;\n" +
            "if (groups.length == 0) { return []; }\n" +
            "var i = 0;\n" +
            "var res = [];\n" +
            "var group;\n" +
            "while (i < groups.length) {\n" +
            "group = groups[i];\n" +
            "res = res + [{\"groupId\": group,\"members\": API.groups.isMember({\"group_id\": group,\"user_ids\": users})}];\n" +
            "i = i + 1;\n" +
            "}\nreturn res;";

    private static final String GROUPS_SEARCH = "var group = \"%s\";\n" +
            "var groupIds = [];\n" +
            "var found = API.groups.search({\"q\":group});\n" +
            "if (found.count == 0) { return []; }\n" +
            "groupIds = groupIds + [found.items[0].id];\n" +
            "if (found.count > 1) { groupIds = groupIds + [found.items[1].id]; }\n" +
            "if (found.count > 2) { groupIds = groupIds + [found.items[2].id]; }\n" +
            "var groupsFull = API.groups.getById({\"group_ids\": groupIds, \"fields\":\"description,members_count,place\"});\n" +
            "return groupsFull;";

    private final VkApiClient vk;

    private final Gson gson;

    private final GeoService geoService;
    private final VkProxyService vkProxyService;

    @Autowired
    public RecommendationService(GeoService geoService, VkProxyService vkProxyService) {
        vk = VkContext.getVkApiClient();
        gson = new GsonBuilder().create();
        this.geoService = geoService;
        this.vkProxyService = vkProxyService;
    }

    public List<GroupInfo> recommendEventByFriends(Float latitude, Float longitude, UserActor actor)
            throws GoogleException, VkException {

        List<GroupInfo> events = getEventsInCity(latitude, longitude, actor);
        if (events.isEmpty()) {
            return events;
        }

        String script = "var result = [];\n";
        for (GroupInfo event : events) {
            script = script + "result = result + [API.groups.getMembers({\"group_id\":\"" + event.getId() + "\"," +
                    "\"filter\":\"friends\"}).items.length];\n";
        }
        script = script + "return result;";
        JsonElement response = null;
        boolean ok = false;
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

        Integer[] friendCounts = gson.fromJson(response, Integer[].class);
        int i = 0;
        for (GroupInfo event : events) {
            event.setFriendsCount(friendCounts[i]);
            i++;
        }

        return events.stream().filter(a -> !a.getFriendsCount().equals(0)).collect(Collectors.toList());
    }

    public List<Integer> getUsersFromCheckins(UserActor actor, List<CheckinInfo> userCheckins) throws VkException {
        if ((userCheckins == null) || (userCheckins.isEmpty())) {
            userCheckins = vkProxyService.getAllUserCheck(actor);
        }
        List<Integer> users = new ArrayList<>();
        String script;
        JsonElement response = null;
        boolean ok = false;
        for (CheckinInfo checkin : userCheckins) {
            if (!checkin.getPlace().getId().equals(0)) {
                ok = false;
                while (!ok) {
                    try {
                        /** Locale.US для точки вместо запятой при подстановке Float */
                        script = String.format(Locale.US, PLACE_CHECKINS_USERS, checkin.getPlace().getId());
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
                if (response.getAsJsonArray().size() != 0) {
                    users.addAll(gson.fromJson(response, new TypeToken<List<Integer>>() {}.getType()));
                }

            } else {
                ok = false;
                while (!ok) {
                    try {
                        script = String.format(Locale.US, COORD_CHECKINS_USERS, checkin.getPlace().getLatitude(),
                                checkin.getPlace().getLongitude());
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
                if (response.getAsJsonArray().size() != 0) {
                    users.addAll(gson.fromJson(response, new TypeToken<List<Integer>>() {}.getType()));
                }
            }
        }

        return users;
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
                    script = String.format(GROUP_MEMBERS, actor.getId(), groupsOffset, currentIds.toString());
                    response = vk.execute().code(actor, script).execute();
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    else {
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
                .sorted((a,b) -> (a.getValue().size() - b.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, HashMap::new));
    }

    public List<UserInfo> getSimilarUsersInfo(UserActor actor, Map<Integer, List<Integer>> userGroups) throws VkException {
        if ((userGroups == null) || (userGroups.isEmpty())) {
            return new ArrayList<>();
        }
        List<Integer> userIds = new ArrayList<>();
        userIds.addAll(userGroups.keySet());

        int start = 0;
        int end = 10000;
        String script;
        JsonElement response = null;
        List<UserFull> users = new ArrayList<>();
        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }
            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 1000;
            end = end + 1000;

            script = "return API.users.get({\"user_ids\":" + currentIds.toString() + ",\"fields\":\"photo_200,schools,career,universities\"});";
            boolean ok = false;
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
            users.addAll(gson.fromJson(response, new TypeToken<List<UserFull>>(){}.getType()));
        }

        return users.stream().map(a -> UserInfo.fromUserFull(a)).collect(Collectors.toList());
    }


    public List<Integer> recommendGroupsByUsers(UserActor actor, Map<Integer, List<Integer>> usersGroups) {
        for (Map.Entry<Integer, List<Integer>> userGroups : usersGroups.entrySet()) {

        }
        return null;
    }

    public List<FullPlaceInfo> recommendNearestPlaces(UserActor actor, Float latitude, Float longitude)
            throws VkException {
        List<PlaceFull> nearestPlaces = null;
        boolean ok = false;
        while (!ok) {
            try {
                nearestPlaces = vk.places().search(actor, latitude, longitude).q("*").radius(1).count(20).execute().getItems();
                if (nearestPlaces.size() == 0) {
                    nearestPlaces = vk.places().search(actor, latitude, longitude).q("*").radius(2).count(20).execute().getItems();
                }
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
        if (nearestPlaces.size() == 0) {
            return new ArrayList<>();
        }

        Map<PlaceFull, Integer> placeRatings = new HashMap<>();
        String scrtipt;
        JsonElement response = null;
        outerLoop: for (PlaceFull place : nearestPlaces) {
            scrtipt = String.format(PLACE_CHECKINS_USERS, place.getId());
            ok = false;
            respLoop: while (!ok) {
                try {
                    response = vk.execute().code(actor, scrtipt).execute();
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(50);
                            continue respLoop;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }
            if (response.getAsJsonArray().size() == 0) {
                continue outerLoop;
            }
            List<Integer> userIds = gson.fromJson(response, new TypeToken<List<Integer>>() {}.getType());

            Map<Integer, List<Integer>> similarUsers = getSimilarUsers(actor, userIds);
            Integer sum = 0;
            for (List<Integer> userGroups : similarUsers.values()) {
                sum = sum + userGroups.size();
            }

            placeRatings.put(place, sum);
        }

        return placeRatings.entrySet().stream()
                .filter(a -> !a.getValue().equals(0))
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(a -> FullPlaceInfo.fromPlaceFull(a.getKey(), a.getValue()))
                .collect(Collectors.toList());

    }

    public List<GroupInfo> recommendGroupsByCheckins(UserActor actor, List<CheckinInfo> checkins) throws VkException {
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
                    response = vk.groups().getById(actor).groupIds(visitedGroupsIds).execute();
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
        loop1: for (String title : allTitles) {
            if (StringUtils.isEmpty(title)) {
                continue loop1;
            }

            script = String.format(GROUPS_SEARCH, title);
            boolean ok = false;
            loop2: while (!ok) {
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

            List<GroupFull> groups = gson.fromJson(response, new TypeToken<List<GroupFull>>(){}.getType());
            result.addAll(groups.stream().map(a -> GroupInfo.fromGroupFull(a)).collect(Collectors.toList()));
        }

        return result;
    }

    public List<GroupInfo> getEventsInCity(Float latitude, Float longitude, UserActor actor) throws VkException,
            GoogleException {
        Integer cityId = geoService.resolveCityId(latitude, longitude, actor);
        if (cityId == null) {
            /** TODO: нужно ли сообщать, что не получилось найти город? */
            return new ArrayList<>();
        }

        String script = String.format(SCRIPT_EVENTS, cityId);
        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        /** TODO: создать десериализатор в GroupInfo */
        List<GroupFull> groups = gson.fromJson(response, new TypeToken<List<GroupFull>>() {
        }.getType());
        if ((groups == null) || (groups.isEmpty())) {
            return new ArrayList<>();
        }

        return groups.stream().map(a -> GroupInfo.fromGroupFull(a)).collect(Collectors.toList());
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

        return TextProcessor.compareTexts(text1, text2);
    }
}
