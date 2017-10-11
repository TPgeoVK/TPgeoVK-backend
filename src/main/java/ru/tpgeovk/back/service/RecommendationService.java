package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.queries.groups.GroupsGetMembersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.GoogleException;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.GroupInfo;
import ru.tpgeovk.back.text.TextProcessor;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final String SCRIPT_EVENTS = "var events = API.groups.search({\"q\":\"*\",\"cityId\":%d,\"count\":25,\"type\":\"event\",\"future\":true,});\n" +
            "var eventIds = events.items@.id;\nreturn API.groups.getById({\"group_ids\":eventIds,\"fields\":\"description,members_count,place\"});\n";

    private final VkApiClient vk;

    private final Gson gson;

    private final GeoService geoService;

    @Autowired
    public RecommendationService(GeoService geoService) {
        vk = VkContext.getVkApiClient();
        gson = new GsonBuilder().create();
        this.geoService = geoService;
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
        JsonElement respone;
        try {
            respone = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            throw new VkException(e.getMessage(), e);
        }

        Integer[] friendCounts = gson.fromJson(respone, Integer[].class);
        int i = 0;
        for (GroupInfo event : events) {
            event.setFriendsCount(friendCounts[i]);
            i++;
        }

        return events.stream().filter(a -> !a.getFriendsCount().equals(0)).collect(Collectors.toList());
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
        List<GroupFull> groups = gson.fromJson(response, new TypeToken<List<GroupFull>>(){}.getType());
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
