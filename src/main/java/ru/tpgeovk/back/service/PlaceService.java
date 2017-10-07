package ru.tpgeovk.back.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.places.PlaceFull;
import com.vk.api.sdk.objects.places.responses.GetCheckinsResponse;
import com.vk.api.sdk.objects.places.responses.SearchResponse;
import com.vk.api.sdk.queries.friends.FriendsGetOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.PlaceInfo;
import ru.tpgeovk.back.text.TextProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final TokenService tokenService;

    private final VkApiClient vk;

    private final TextProcessor textProcessor = new TextProcessor();

    @Autowired
    public PlaceService(TokenService tokenService) {
        this.tokenService = tokenService;
        vk = VkContext.getVkApiClient();
    }

    public List<PlaceInfo> getPlaces(Float lat, Float lon, String text, UserActor actor)
            throws VkException {

        /** Получаем все места в радиусе 300 метров */
        SearchResponse response = null;
        try {
            response = vk.places().search(actor, lat, lon)
                    .radius(1)
                    .execute();
        } catch (ApiException | ClientException e) {
            throw new VkException(e);
        }

        /** Сортируем по расстоянию и преобразовываем в PlaceInfo */
        List<PlaceInfo> places = response.getItems().stream()
                .sorted(Comparator.comparing(PlaceFull::getDistance))
                .map(PlaceInfo::fromPlaceFull)
                .collect(Collectors.toList());

        /** Получаем список друзей пользователя */
        GetResponse friendsResponse = null;
        try {
            friendsResponse = vk.friends().get(actor)
                    .order(FriendsGetOrder.HINTS)
                    .execute();
        } catch (ApiException | ClientException  e) {
            throw new VkException(e);
        }

        GetCheckinsResponse checkinsResponse = null;
        try {
            /** Считаем количество чекинов друзей и пользователя в каждом месте */
            for (PlaceInfo place : places) {
                /** Issue #80 */
                /*checkinsResponse = vk.places().getCheckins(actor)
                        .place(place.getId())
                        .execute();

                for (Checkin checkin : checkinsResponse.getItems()) {
                    if (friendsResponse.getItems().contains(checkin.getUserId())) {
                        place.updateFriendsCheckinsCount();
                    }
                    if (checkin.getUserId().equals(actor.getId())) {
                        place.updateUserCheckinsCount();
                    }
                } */

                /** За одно считаем и встерчаемость названия места в тексте */
                place.setTextRating(textProcessor.fuzzyContainRating(place.getTitle(), text));
            }

        } catch (Exception e) {
            throw new VkException(e);
        }

        return places;
    }

    public  PlaceInfo predictPlace(List<PlaceInfo> places) {
        return places.stream().max(Comparator.comparing(PlaceInfo::calculateRating)).get();
    }
}
