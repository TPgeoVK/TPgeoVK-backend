package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.users.UserField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tpgeovk.back.contexts.GsonContext;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.model.UserInfo;
import ru.tpgeovk.back.model.deserializer.UserFeaturesDeserializer;
import ru.tpgeovk.back.model.vk.VkWallpost;
import ru.tpgeovk.back.model.vk.VkWallpostFull;
import ru.tpgeovk.back.scripts.VkScripts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class VkProxyService {

    private static final long QUARTER_OFFSET = 300; //5 min.

    private final VkApiClient vk;

    private final Gson gson;

    @Autowired
    public VkProxyService() {
        this.vk = VkContext.getVkApiClient();
        gson = GsonContext.createGson();
    }

    public UserInfo getUser(UserActor actor) throws VkException {
        List<UserXtrCounters> usersResponse;
        try {
            usersResponse = vk.users().get(actor).userIds(actor.getId().toString())
                    .fields(UserField.PHOTO_200, UserField.CAREER, UserField.UNIVERSITIES, UserField.SCHOOLS).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        return UserInfo.fromXtrCounters(usersResponse.get(0));
    }

    public List<CheckinInfo> getAllUserCheckins(UserActor actor) throws VkException {
        /** TODO: replace with private static final String */
        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append(
                "var checkins = API.places.getCheckins({\"user_id\":" + actor.getId().toString() + ",\"count\":100});\n")
                .append("var count = checkins.count;\n")
                .append("var allCheckins = checkins.items;\n")
                .append("if (checkins.length == 100) {\n")
                .append("var offset = 100;\n")
                .append("checkins = API.places.getCheckins({\"user_id\":" + actor.getId().toString() + ",\"count\":100, " +
                        "\"offset\":offset});\n")
                .append("while (checkins.length != 0) {\n")
                .append("allCheckins = allCheckins + checkins.items;\n")
                .append("offset = offset + 100;\n")
                .append("checkins = API.places.getCheckins({\"user_id\":" + actor.getId().toString() + ",\"count\":100, " +
                        "\"offset\":offset});\n")
                .append("}\n}\n")
                .append("if (allCheckins.length == 0) { return []; }\n")
                .append("var posts = API.wall.getById({\"posts\":allCheckins@.id});\n")
                .append("return posts;");

        String script = scriptBuilder.toString();

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

        List<VkWallpostFull> posts;
        if (response.getAsJsonArray().size() != 0) {
            posts = gson.fromJson(response, new TypeToken<List<VkWallpostFull>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }

        return posts.stream().map(a -> CheckinInfo.fromPostFull(a)).collect(Collectors.toList());
    }

    public List<CheckinInfo> getLatestCheckins(UserActor actor, Float latitude, Float longitude) throws VkException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long offsetTime = timestamp.getTime() - QUARTER_OFFSET;

        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append(
                "var checkins = API.places.getCheckins({\"latitude\":" + latitude.toString() + ",\"longitude\": "
                        + longitude.toString() + "});\n")
                .append("if (checkins.count == 0) { return [[],[]]; }\n")
                .append("var posts = API.wall.getById({\"posts\":checkins.items@.id});\n")
                .append("var users = API.users.get({\"user_ids\":posts@.from_id,\"fields\":\"photo_200,schools,career,universities\"});")
                .append("return [posts,users];");

        String script = scriptBuilder.toString();

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

        List<VkWallpostFull> posts;
        if (response.getAsJsonArray().size() != 0) {
            posts = gson.fromJson(response.getAsJsonArray().get(0), new TypeToken<List<VkWallpostFull>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }

        List<CheckinInfo> result = posts.stream().map(a -> CheckinInfo.fromPostFull(a)).collect(Collectors.toList());

        UserFull users[] = gson.fromJson(response.getAsJsonArray().get(1), UserFull[].class);
        int i = 0;
        for (CheckinInfo checkinInfo : result) {
            checkinInfo.setUser(UserInfo.fromUserFull(users[i]));
            i++;
        }

        return result;
    }

    public CheckinInfo createPost(UserActor actor, Integer placeId, String text) throws VkException {
        JsonElement response = null;
        String script = StringUtils.isEmpty(text) ? String.format(VkScripts.CREATE_CHECKIN, actor.getId(), placeId) :
                String.format(VkScripts.CREATE_CHECKIN_TEXT, actor.getId(), placeId, text);

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
        Integer postId = response.getAsInt();

        script = "return API.wall.getById({\"posts\":\"" + actor.getId().toString() + "_" + postId.toString() + "\"})[0];";
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
                } else {
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }
        }
        VkWallpostFull post = gson.fromJson(response, VkWallpostFull.class);

        return CheckinInfo.fromPostFull(post);
    }

    public List<UserInfo> getUsers(UserActor actor, List<Integer> userIds) throws VkException {
        int start = 0;
        int end = 1000;
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

            script = "return API.users.get({\"user_ids\":" + currentIds.toString()
                    + ",\"fields\":\"photo_200,schools,career,universities\"});";
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
            users.addAll(gson.fromJson(response, new TypeToken<List<UserFull>>() {}.getType()));
        }

        return users.stream().map(a -> UserInfo.fromUserFull(a)).collect(Collectors.toList());
    }

    public List<UserInfo> getUsers(UserActor actor, List<Integer> userIds, String reason) throws VkException {
        int start = 0;
        int end = 1000;
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

            script = "return API.users.get({\"user_ids\":" + currentIds.toString()
                    + ",\"fields\":\"photo_200,schools,career,universities\"});";
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
            users.addAll(gson.fromJson(response, new TypeToken<List<UserFull>>() {}.getType()));
        }

        return users.stream().map(a -> UserInfo.fromUserFull(a, reason)).collect(Collectors.toList());
    }

    public List<Integer> getUsersFromCheckins(UserActor actor, List<CheckinInfo> userCheckins) throws VkException {
        if ((userCheckins == null) || (userCheckins.isEmpty())) {
            userCheckins = getAllUserCheckins(actor);
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
                        script = String.format(Locale.US, VkScripts.PLACE_CHECKINS_USERS, checkin.getPlace().getId());
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
                    users.addAll(gson.fromJson(response, new TypeToken<List<Integer>>() {
                    }.getType()));
                }

            } else {
                ok = false;
                while (!ok) {
                    try {
                        script = String.format(Locale.US, VkScripts.COORD_CHECKINS_USERS, checkin.getPlace().getLatitude(),
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

        users = users.stream()
                .filter(a -> !actor.getId().equals(a))
                .distinct()
                .collect(Collectors.toList());

        return filterBanned(actor, users);
    }

    public List<Integer> filterBanned(UserActor actor, List<Integer> userIds) throws VkException {
        boolean ok;
        JsonElement response = null;
        String script;

        int start = 0;
        int end = 1000;
        List<Integer> result = new ArrayList<>();

        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }

            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 1000;
            end = end + 1000;

            script = String.format(VkScripts.FILTER_DEACTIVATED_USERS, currentIds.toString());
            ok = false;
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(100);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }

            JsonArray responseArray = response.getAsJsonArray();
            int i = 0;
            for (JsonElement element : responseArray) {
                if (element.isJsonNull()) {
                    result.add(currentIds.get(i));
                }
                i = i + 1;
            }
        }

        return result;
    }

    public List<UserFeatures> getUsersFeatures(UserActor actor, List<Integer> userIds) throws VkException {
        boolean ok;
        JsonElement response = null;
        String script;

        int start = 0;
        int end = 24;
        List<UserFeatures> result = new ArrayList<>();

        while (start < userIds.size()) {
            if (end > userIds.size()) {
                end = userIds.size();
            }

            List<Integer> currentIds = userIds.subList(start, end);
            start = start + 24;
            end = end + 24;

            script = String.format(VkScripts.GET_USERS_FEATURES, currentIds.toString());
            ok = false;
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(100);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }

            List<UserFeatures> users = gson.fromJson(response, new TypeToken<List<UserFeatures>>(){}.getType());
            result.addAll(users);
        }

        return result;
    }

    public UserFeatures getActorFeatures(UserActor actor) throws VkException {
        JsonElement response = null;
        String script = String.format(VkScripts.GET_USER_FEATURES, actor.getId());
        boolean ok = false;
        while (!ok) {
            try {
                response = vk.execute().code(actor, script).execute();
                ok = true;
            } catch (ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(100);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                e.printStackTrace();
                throw new VkException(e.getMessage(), e);
            }
        }

        UserFeatures actorFeatures = gson.fromJson(response, UserFeatures.class);
        return actorFeatures;
    }
}

