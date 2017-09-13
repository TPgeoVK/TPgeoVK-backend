package ru.tpgeovk.back.ru.tpgeovk.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tpgeovk.back.ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.ru.tpgeovk.back.model.PlaceInfo;
import ru.tpgeovk.back.ru.tpgeovk.back.model.request.PredictRequest;
import ru.tpgeovk.back.ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.ru.tpgeovk.back.service.PlaceDetectionService;

import java.util.List;

@RestController
public class PlacesController {

    private final PlaceDetectionService placeDetectionService;

    @Autowired
    public PlacesController(PlaceDetectionService placeDetectionService) {
        this.placeDetectionService = placeDetectionService;
    }

    @RequestMapping(path = "/places/predict", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getPredictedPlace(@RequestBody PredictRequest request) {

        List<PlaceInfo> places = null;

        try {
            places = placeDetectionService.getPlaces(request.getUserId(), request.getLatitude(),
                    request.getLongitude(), request.getText());
        } catch (VkException e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ErrorResponse(e.getMessage()));
        }

        if (places == null) {
            return ResponseEntity.ok(new ErrorResponse("No places near your location!"));
        }

        PlaceInfo predictedPlace = placeDetectionService.predictPlace(places);

        return ResponseEntity.ok(predictedPlace);
    }
}
