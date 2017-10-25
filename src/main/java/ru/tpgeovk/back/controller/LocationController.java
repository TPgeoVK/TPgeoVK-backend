package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.request.PredictRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.PlaceService;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.UsersDataService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LocationController {

    private final TokenService tokenService;
    private final PlaceService placeService;
    private final RecommendationService recommendationService;
    private final UsersDataService usersDataService;

    @Autowired
    public LocationController(TokenService tokenService,
                              PlaceService placeDetectionService,
                              RecommendationService recommendationService,
                              UsersDataService usersDataService) {
        this.tokenService =tokenService;
        this.placeService = placeDetectionService;
        this.recommendationService = recommendationService;
        this.usersDataService = usersDataService;
    }

    @RequestMapping(path = "/location/detectPlace", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getPredictedPlace(@RequestBody PredictRequest request) {

        UserActor actor = tokenService.getUser(request.getToken());
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        List<FullPlaceInfo> nearestPlaces = new ArrayList<>();
        List<FullPlaceInfo> storedPlaces = usersDataService.getRecommendedNearestPlaces(actor.getAccessToken());
        if (((storedPlaces != null) && (storedPlaces.size() != 0)) || ((request.getLatitude() == null) ||
                (request.getLongitude() == null))) {
            nearestPlaces.addAll(storedPlaces);
        } else {
            try {
                nearestPlaces = recommendationService.recommendNearestPlaces(actor, request.getLatitude(),
                        request.getLongitude());
                usersDataService.getRecommendedNearestPlaces(actor.getAccessToken()).addAll(nearestPlaces);
            } catch (VkException e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            }
        }

        FullPlaceInfo predictedPlace = placeService.detectPlace(actor, nearestPlaces, request.getText());

        return ResponseEntity.ok(predictedPlace);
    }
}
