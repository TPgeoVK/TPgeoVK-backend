package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.users.UserField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserInfo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VkProxyService {

    private static final long QUARTER_OFFSET = 900; //15 min.

    private final VkApiClient vk;

    private final Gson gson;

    @Autowired
    public VkProxyService() {
        this.vk = VkContext.getVkApiClient();
        gson = new GsonBuilder().create();
    }

    public List<CheckinInfo> getAllUserCheck(UserActor actor) throws VkException {
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
        .append("var user = API.users.get({\"user_ids\":" + actor.getId().toString() + ",\"fields\":\"photo_200\"});\n")
        .append("if (allCheckins.length == 0) { return [[],user]; }\n")
        .append("var posts = API.wall.getById({\"posts\":allCheckins@.id});\n")
        .append("return [posts,user];");

        String script = scriptBuilder.toString();

        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        List<WallpostFull> posts;
        if (response.getAsJsonArray().size() != 0) {
            posts = Arrays.asList(gson.fromJson(response.getAsJsonArray().get(0), WallpostFull[].class));
        }
        else {
            posts = new ArrayList<>();
        }

        UserFull user = gson.fromJson(response.getAsJsonArray().get(1).getAsJsonArray().get(0), UserFull.class);

        return posts.stream().map(a -> CheckinInfo.fromPostAndUser(a, user)).collect(Collectors.toList());
    }

    public List<CheckinInfo> getLatestCheckins(UserActor actor, Float latitude, Float longitude) throws VkException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long offsetTime = timestamp.getTime() - QUARTER_OFFSET;

        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append(
                "var checkins = API.places.getCheckins({\"latitude\":" + latitude.toString() + ",\"longitude\": "
                        + longitude.toString() + ",\"timestamp\":" + String.valueOf(offsetTime) + ",\"count\":100});\n")
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
                .append("if (checkins.length == 0) { return [[],[]]; }\n")
                .append("var posts = API.wall.getById({\"posts\":allCheckins@.id});\n")
                .append("var users = API.users.get({\"user_ids\":posts@.from_id,\"fields\":\"photo_200\"});")
                .append("return [posts,users];");

        String script = scriptBuilder.toString();

        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        List<WallpostFull> posts;
        if (response.getAsJsonArray().size() != 0) {
            posts = Arrays.asList(gson.fromJson(response.getAsJsonArray().get(0), WallpostFull[].class));
        }
        else {
            return new ArrayList<>();
        }

        List<CheckinInfo> result = posts.stream().map(a -> CheckinInfo.fromPostFull(a)).collect(Collectors.toList());

        UserFull users[] = gson.fromJson(response.getAsJsonArray().get(1), UserFull[].class);
        int i = 0;
        for (CheckinInfo checkinInfo : result) {
            checkinInfo.setUser(new UserInfo(
                    users[i].getId(),
                    users[i].getFirstName(),
                    users[i].getLastName(),
                    users[i].getPhoto200()));
            i++;
        }

        return result;
    }
}

