package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.PlaceInfo;
import ru.tpgeovk.back.model.request.PredictRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LocationController {

    private final TokenService tokenService;
    private final PlaceService placeService;
    private final RecommendationService recommendationService;
    private final VkProxyService vkProxyService;

    @Autowired
    public LocationController(TokenService tokenService,
                              PlaceService placeDetectionService,
                              RecommendationService recommendationService,
                              VkProxyService vkProxyService) {
        this.tokenService = tokenService;
        this.placeService = placeDetectionService;
        this.recommendationService = recommendationService;
        this.vkProxyService = vkProxyService;
    }

    @RequestMapping(path = "/location/detectPlace", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getPredictedPlace(@RequestBody PredictRequest request) {

        UserActor actor = tokenService.getUser(request.getToken());
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        try {
            List<FullPlaceInfo> nearestPlaces = recommendationService.recommendNearestPlaces(actor, request.getLatitude(),
                    request.getLongitude());
            FullPlaceInfo predictedPlace = placeService.detectPlace(actor, nearestPlaces, request.getText());

            return ResponseEntity.ok(predictedPlace);
        } catch (VkException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
