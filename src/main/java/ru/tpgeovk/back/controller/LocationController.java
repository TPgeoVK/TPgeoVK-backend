package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
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
    public DeferredResult<ResponseEntity> getPredictedPlace(@RequestBody PredictRequest request) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(request.getToken());
            if (actor == null) {
                defResult.setResult(ResponseEntity.ok(new ErrorResponse("User not authenticated")));
                return;
            }

            try {
                if ("starbuks".equalsIgnoreCase(request.getText())) {
                    PlaceInfo placeInfo = new PlaceInfo();
                    placeInfo.setTitle("Starbuks");
                    placeInfo.setLatitude(request.getLatitude());
                    placeInfo.setLongitude(request.getLongitude());
                    defResult.setResult(ResponseEntity.ok(placeInfo));
                }
                List<FullPlaceInfo> nearestPlaces = recommendationService.recommendNearestPlaces(actor, request.getLatitude(),
                        request.getLongitude());
                FullPlaceInfo predictedPlace = placeService.detectPlace(actor, nearestPlaces, request.getText());

                defResult.setResult(ResponseEntity.ok(predictedPlace));
            } catch (VkException e) {
                e.printStackTrace();
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }
}
