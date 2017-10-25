package ru.tpgeovk.back.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Country;
import com.vk.api.sdk.objects.database.responses.GetCitiesResponse;
import com.vk.api.sdk.objects.database.responses.GetCountriesResponse;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.contexts.GoogleContext;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.GoogleException;
import ru.tpgeovk.back.exception.NoCityException;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CityCountry;

import java.io.IOException;
import java.util.Optional;

@Service
public class GeoService {

    private final GeoApiContext geoApiContext;
    private final VkApiClient vk;

    public GeoService() {
        this.geoApiContext = GoogleContext.getGeoApiContext();
        this.vk = VkContext.getVkApiClient();
    }

    public CityCountry decodeCoordinates(Float latitude, Float longitude) throws GoogleException {
        GeocodingApiRequest request = GeocodingApi.newRequest(geoApiContext)
                .latlng(new LatLng(latitude, longitude))
                .resultType(AddressType.LOCALITY)
                .language("ru");

        GeocodingResult[] geoResponse;
        try {
            geoResponse = request.await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            throw new GoogleException(e);
        }

        if (geoResponse.length == 0) {
            return null;
        }

        int length = geoResponse[0].addressComponents.length;
        String city = geoResponse[0].addressComponents[0].shortName;
        String country = geoResponse[0].addressComponents[length-1].longName;

        return new CityCountry(city, country);
    }

    public Integer resolveCityId(String city, String country, UserActor actor) throws VkException {
        GetCountriesResponse countriesResponse = null;
        boolean ok = false;
        while (!ok) {
            try {
                /** TODO: закэшировать в key-value хранилище */
                countriesResponse = vk.database().getCountries(actor).execute();
                ok = true;
            } catch (com.vk.api.sdk.exceptions.ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(50);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    throw new VkException(e.getMessage(), e);
                }
            }
        }
        Optional<Country> countrySearch = countriesResponse.getItems().stream()
                .filter((Country a) -> a.getTitle().toLowerCase().equals(country.toLowerCase()))
                .findFirst();
        if (!countrySearch.isPresent()) {
            return null;
        }
        int countryId = countrySearch.get().getId();

        GetCitiesResponse citiesResponse = null;
        ok = false;
        while (!ok) {
            try {
                citiesResponse = vk.database().getCities(actor, countryId).q(city).count(1).execute();
                ok = true;
            } catch (com.vk.api.sdk.exceptions.ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(50);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    throw new VkException(e.getMessage(), e);
                }
            }
        }
        if (citiesResponse.getItems().isEmpty()) {
            return null;
        }

        return citiesResponse.getItems().get(0).getId();
    }

    public Integer resolveCityId(Float latitude, Float longitude, UserActor actor) throws GoogleException,
            VkException {
        CityCountry cityCountry = decodeCoordinates(latitude, longitude);
        if (cityCountry == null) {
            return null;
        }

        return resolveCityId(cityCountry.getCity(), cityCountry.getCountry(), actor);
    }
}
