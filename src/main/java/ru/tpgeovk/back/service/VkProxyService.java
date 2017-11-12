package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserInfo;
import ru.tpgeovk.back.model.vk.VkWallpost;
import ru.tpgeovk.back.model.vk.VkWallpostFull;
import ru.tpgeovk.back.scripts.VkScripts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VkProxyService {

    private static final long QUARTER_OFFSET = 300; //5 min.

    private final VkApiClient vk;

    private final Gson gson;

    @Autowired
    public VkProxyService() {
        this.vk = VkContext.getVkApiClient();
        gson = new GsonBuilder().create();
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
            posts = gson.fromJson(response, new TypeToken<List<VkWallpostFull>>(){}.getType());
        }
        else {
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
            posts = gson.fromJson(response.getAsJsonArray().get(0), new TypeToken<List<VkWallpostFull>>(){}.getType());
        }
        else {
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
}

