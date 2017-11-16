package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.PlaceInfo;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.model.request.PredictRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LocationController {

    private final TokenService tokenService;
    private final PlaceService placeService;
    private final LocationService locationService;
    private final VkProxyService vkProxyService;

    @Autowired
    public LocationController(TokenService tokenService,
                              PlaceService placeDetectionService,
                              LocationService locationService,
                              VkProxyService vkProxyService) {
        this.tokenService = tokenService;
        this.placeService = placeDetectionService;
        this.locationService = locationService;
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
                FullPlaceInfo place = locationService.detectPlace(actor, request.getLatitude(), request.getLongitude(),
                        request.getText());
                defResult.setResult(ResponseEntity.ok(place));
            } catch (VkException e) {
                e.printStackTrace();
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }
}
