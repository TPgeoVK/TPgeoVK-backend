package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserInfo;
import ru.tpgeovk.back.model.request.CreatePostRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.VkProxyService;

import java.awt.geom.FlatteningPathIterator;
import java.util.List;

@RestController
public class VkProxyController {

    private final TokenService tokenService;
    private final VkProxyService vkService;

    @Autowired
    public VkProxyController(TokenService tokenService,
                          VkProxyService vkService) {
        this.tokenService = tokenService;
        this.vkService = vkService;
    }

    @RequestMapping(path = "/vkapi/user", method = RequestMethod.GET)
    public ResponseEntity getUser(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received"));
        }

        UserInfo result;
        try {
            result = vkService.getUser(actor);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(path = "/vkapi/checkins/all", method = RequestMethod.GET)
    public ResponseEntity getAllCheckins(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received"));
        }

        List<CheckinInfo> result;
        try {
            result = vkService.getAllUserCheck(actor);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(path = "/vkapi/checkins/latest", method = RequestMethod.GET)
    public ResponseEntity getLatestCheckins(@RequestParam(value = "token") String token,
                                            @RequestParam(value = "latitude") String lat,
                                            @RequestParam(value = "longitude") String lon) {

        Float latitude;
        Float longitude;
        try {
            latitude = Float.parseFloat(lat);
            longitude = Float.parseFloat(lon);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Wrong coordinates values!"));
        }

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received"));
        }

        List<CheckinInfo> result;
        try {
            result = vkService.getLatestCheckins(actor, latitude, longitude);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(path = "/vkapi/post/create", method = RequestMethod.POST)
    public ResponseEntity createPost(@RequestBody CreatePostRequest request) {

        UserActor actor = tokenService.getUser(request.getToken());
        if (actor == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received"));
        }

        try {
            CheckinInfo result = vkService.createPost(actor, request.getPlaceId(), request.getText());
            return ResponseEntity.ok(result);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
