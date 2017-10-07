package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.VkContext;
import ru.tpgeovk.back.model.GroupInfo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    /** TODO: добавить нормальную обработку исключений */

    private final VkApiClient vk;

    private Gson gson = new Gson();

    @Autowired
    public RecommendationService() {
        vk = VkContext.getVkApiClient();
    }

    public List<GroupInfo> recommendEventByFriends(String city, String country, UserActor actor) {
        List<GroupInfo> events = getEventsInCity(city, country, actor);
        if (events == null) {
            return null;
        }

        /** Этап 1. Ищем события, в которых участвуют друзья или сам пользователь. */
        /** Этап 2. Ищем наиболее близкие события к событиям пользователя на основе
         * какой-либо меры (косинусное расстояние и т.п.). Наиболее важные поля -
         * название события, описание и количество участников.
         */

        /** Получаем список друзей пользователя */
        GetResponse friendsResponse = null;
        try {
            friendsResponse = vk.friends().get(actor).count(500).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
        List<Integer> friendsIds = friendsResponse.getItems();

        /** Смотрим, сколько друзей в каждом мероприятии */
        for (GroupInfo event : events) {
            Long count = 0L;
            try {
                count = vk.groups().isMember(actor, event.getId(), friendsIds).execute().stream()
                        .filter(a -> a.isMember()).count();
                Thread.currentThread().sleep(500);
            } catch (ApiException | ClientException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            event.setFriendsCount(count);
        }

        return events.stream()
                .filter(a -> !a.getFriendsCount().equals(Long.valueOf(0)))
                .collect(Collectors.toList());
    }

    public List<String> recommendEventByStory(String city, String country, UserActor actor) {
        List<GroupInfo> events = getEventsInCity(city, country, actor);
        if (events == null) {
            return null;
        }

        String eventIds = events.stream().map(a -> a.getId()).collect(Collectors.joining(","));
        String executeCode = "var res = API.groups.getById({\"group_ids\": " + eventIds +
                ", \"fields\": \"name,description,place\"}); return res;";
        JsonElement respone = null;
        try {
            respone = vk.execute().code(actor, executeCode).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public List<GroupInfo> getEventsInCity(String city, String country, UserActor actor) {
        Integer cityId = getIdByCity(city, country, actor);
        if (cityId == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        String script = builder
                .append("var events = API.groups.search({\"q\":\"*\",\"cityId\":" + cityId.toString() +
                        ",\"count\":10,\"type\":\"event\",\"future\":true});\n")
                .append("var eventIds = events.items@.id;\n")
                .append("return API.groups.getById({\"group_ids\":eventIds});\n")
                .toString();
        JsonElement response = null;
        try {
            response = vk.execute().code(actor, script).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }

        return null;

        /*
        SearchResponse searchResponse = null;
        /** TODO: получить GroupFull через execute
        try {
            /** Ищем все события в городе
            searchResponse = vk.groups().search(actor, "*")
                    .cityId(cityId)
                    .count(10)
                    .type("event")
                    .future(true)
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
        if (searchResponse.getItems().isEmpty()) {
            return null;
        }
        List<GroupInfo> events = searchResponse.getItems().stream()
                .filter(a -> a.getIsClosed().equals(GroupIsClosed.OPEN))
                .map(a -> GroupInfo.fromGroup(a))
                .collect(Collectors.toList());

        return events; */
    }

    public Integer getIdByCity(String city, String country, UserActor actor) {
        GetCountriesResponse countriesResponse = null;
        try {
            /** TODO: cache! */
            countriesResponse = vk.database().getCountries(actor).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
        Optional<Country> countrySearch = countriesResponse.getItems().stream()
                .filter((Country a) -> a.getTitle().toLowerCase().equals(country.toLowerCase()))
                .findFirst();
        if (!countrySearch.isPresent()) {
            return null;
        }
        int countryId = countrySearch.get().getId();

        GetCitiesResponse citiesResponse = null;
        try {
            citiesResponse = vk.database().getCities(actor, countryId).q(city).count(1).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
        if (citiesResponse.getItems().isEmpty()) {
            return null;
        }

        return citiesResponse.getItems().get(0).getId();
    }
}
