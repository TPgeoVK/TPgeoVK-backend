package ru.tpgeovk.back.ru.service;

import com.vk.api.sdk.actions.Search;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.groups.GroupIsClosed;
import com.vk.api.sdk.objects.groups.responses.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.VkContext;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final TokenService tokenService;
    private final VkApiClient vk;

    @Autowired
    public RecommendationService(TokenService tokenService) {
        this.tokenService = tokenService;
        vk = VkContext.getVkApiClient();
    }

    public List<String> recommendEventByCity(String city, String country, UserActor actor) {
        Integer cityId = getIdByCity(city, country, actor);
        if (cityId == null) {
            return null;
        }

        SearchResponse searchResponse = null;
        try {
            /** Ищем все события в городе */
            searchResponse = vk.groups().search(actor, "*")
                    .cityId(cityId)
                    .count(100)
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
        List<Group> events = searchResponse.getItems().stream()
                .filter(a -> a.getIsClosed().equals(GroupIsClosed.OPEN))
                .collect(Collectors.toList());

        /** Этап 1. Ищем события, в которых участвуют друзья или сам пользователь. */
        /** Этап 2. Ищем наиболее близкие события к событиям пользователя на основе
         * какой-либо меры (косинусное расстояние и т.п.). Наиболее важные поля -
         * название события, описание и количество участников.
         */

        /** Получаем список друзей пользователя */
        GetResponse friendsResponse = null;
        try {
            friendsResponse = vk.friends().get(actor).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return null;
        }
        List<Integer> friendsIds = friendsResponse.getItems();
        Map<String, Long> friendsCount = new HashMap<>();

        /** Смотрим, сколько друзей в каждом мероприятии */
        /** TODO: исправить на vk.execute() */
        for (Group event : events) {
            Long count = 0L;
            int limit = friendsIds.size() / 500;
            int step = 0;
            for (step = 0; step < limit; step++) {
                try {
                    count = count + vk.groups().isMember(actor, event.getId(),
                            friendsIds.subList(500*step, 500*(step+1))).execute()
                            .stream().filter(a -> a.isMember()).count();
                    friendsCount.put(event.getId(), count);
                } catch (ApiException | ClientException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            try {
                count = count + vk.groups().isMember(actor, event.getId(),
                        friendsIds.subList(500*step, friendsIds.size())).execute()
                        .stream().filter(a -> a.isMember()).count();
                friendsCount.put(event.getId(), count);
            } catch (ApiTooManyException e) {
                try {
                    Thread.currentThread().sleep(2000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            } catch (ApiException | ClientException e) {
                //e.printStackTrace();
                //return null;
            }
        }

        return friendsCount.keySet().stream()
                .filter(a -> (!friendsCount.get(a).equals(0L)))
                .collect(Collectors.toList());
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
