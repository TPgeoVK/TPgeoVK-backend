package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;

import java.sql.Timestamp;
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
        .append("var posts = API.wall.getById({\"posts\":allCheckins@.id});\n")
        .append("return posts;");

        String script = scriptBuilder.toString();

        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        List<WallpostFull> posts = Arrays.asList(gson.fromJson(response, WallpostFull[].class));

        return posts.stream().map(a -> CheckinInfo.fromPostFull(a)).collect(Collectors.toList());
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
                .append("var posts = API.wall.getById({\"posts\":allCheckins@.id});\n")
                .append("return posts;");

        String script = scriptBuilder.toString();

        JsonElement response;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        List<WallpostFull> posts = Arrays.asList(gson.fromJson(response, WallpostFull[].class));

        return posts.stream().map(a -> CheckinInfo.fromPostFull(a)).collect(Collectors.toList());
    }
}

