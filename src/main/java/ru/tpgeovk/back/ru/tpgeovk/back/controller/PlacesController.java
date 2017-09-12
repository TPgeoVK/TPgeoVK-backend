package ru.tpgeovk.back.ru.tpgeovk.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpgeovk.back.ru.tpgeovk.back.service.PlaceDetectionService;

@RestController
public class PlacesController {

    private final PlaceDetectionService placeDetectionService;

    @Autowired
    public PlacesController(PlaceDetectionService placeDetectionService) {
        this.placeDetectionService = placeDetectionService;
    }

    /* @RequestMapping(path = "/places/predict", method = RequestMethod.GET)
    public ResponseEntity getPredictedPlace() {

    } */
}
