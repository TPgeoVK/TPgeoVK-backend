package ru.tpgeovk.back.ru.service;

import com.vk.api.sdk.actions.Search;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import com.vk.api.sdk.objects.groups.responses.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tpgeovk.back.VkContext;

import java.util.Optional;

public class RecommendationService {

    private final TokenService tokenService;
    private final VkApiClient vk;

    @Autowired
    public RecommendationService(TokenService tokenService) {
        this.tokenService = tokenService;
        vk = VkContext.getVkApiClient();
    }

    public Integer recommendEventByCity(String city, String country, UserActor actor) {
        Integer cityId = getIdByCity(city, country, actor);
        if (cityId == null) {
            return null;
        }

        SearchResponse searchResponse = null;
        try {
            /** Ищем все события в городе */
            searchResponse = vk.groups().search(actor, "*")
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

        /** Этап 1. Ищем события, в которых участвуют друзья или сам пользователь. */
        /** Этап 2. Ищем наиболее близкие события к событиям пользователя на основе
         * какой-либо меры (косинусное расстояние и т.п.). Наиболее важные поля -
         * название события, описание и количество участников.
         */

        return null;
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
