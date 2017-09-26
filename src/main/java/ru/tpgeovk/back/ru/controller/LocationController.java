package ru.tpgeovk.back.ru.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tpgeovk.back.ru.exception.VkException;
import ru.tpgeovk.back.ru.model.PlaceInfo;
import ru.tpgeovk.back.ru.model.request.PredictRequest;
import ru.tpgeovk.back.ru.model.response.ErrorResponse;
import ru.tpgeovk.back.ru.service.PlaceService;
import java.util.List;

@RestController
public class LocationController {

    private final PlaceService placeService;

    @Autowired
    public LocationController(PlaceService placeDetectionService) {
        this.placeService = placeDetectionService;
    }

    @RequestMapping(path = "/location/predictPlace", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getPredictedPlace(@RequestBody PredictRequest request) {

        List<PlaceInfo> places = null;

        try {
            places = placeService.getPlaces(request.getUserId(), request.getLatitude(),
                    request.getLongitude(), request.getText());
        } catch (VkException e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ErrorResponse(e.getMessage()));
        }

        if (places == null) {
            return ResponseEntity.ok(new ErrorResponse("No places near your location!"));
        }

        PlaceInfo predictedPlace = placeService.predictPlace(places);

        return ResponseEntity.ok(predictedPlace);
    }
}
