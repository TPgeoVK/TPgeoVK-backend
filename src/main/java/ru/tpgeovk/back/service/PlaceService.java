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
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.text.TextProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final TokenService tokenService;
    private final UsersDataService usersDataService;

    private final VkApiClient vk;

    private final TextProcessor textProcessor = new TextProcessor();

    @Autowired
    public PlaceService(TokenService tokenService,
                        UsersDataService usersDataService) {
        this.tokenService = tokenService;
        this.usersDataService = usersDataService;
        vk = VkContext.getVkApiClient();
    }

    public FullPlaceInfo detectPlace(UserActor actor, List<FullPlaceInfo> nearestPlaces, String text) {
        nearestPlaces.forEach(a -> a.setTextRating(textProcessor.fuzzyContainRating(a.getTitle(), text)));

        return nearestPlaces.stream()
                .max(Comparator.comparing(FullPlaceInfo::calculateRating))
                .get();
    }
}
