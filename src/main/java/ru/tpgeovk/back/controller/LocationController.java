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
import ru.tpgeovk.back.service.TokenService;

import java.util.List;

@RestController
public class LocationController {

    private final TokenService tokenService;
    private final PlaceService placeService;

    @Autowired
    public LocationController(TokenService tokenService, PlaceService placeDetectionService) {
        this.tokenService =tokenService;
        this.placeService = placeDetectionService;
    }

    @RequestMapping(path = "/location/detectPlace", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getPredictedPlace(@RequestBody PredictRequest request) {

        Integer userId = tokenService.getUserId(request.getToken());
        if (userId == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }
        UserActor actor = new UserActor(userId, request.getToken());

        List<FullPlaceInfo> places = null;

        try {
            places = placeService.getPlaces(request.getLatitude(), request.getLongitude(),
                    request.getText(), actor);
        } catch (VkException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        if (places == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("No places near your location!"));
        }

        FullPlaceInfo predictedPlace = placeService.predictPlace(places);

        return ResponseEntity.ok(predictedPlace);
    }
}
