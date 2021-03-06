package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.*;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.VkProxyService;

import java.util.List;
import java.util.Map;

@RestController
public class RecommendController {

    private final TokenService tokenService;
    private final RecommendationService recommendationService;
    private final VkProxyService vkProxyService;

    @Autowired
    public RecommendController(TokenService tokenService,
                               RecommendationService recommendationService,
                               VkProxyService vkProxyService) {
        this.tokenService = tokenService;
        this.recommendationService = recommendationService;
        this.vkProxyService = vkProxyService;
    }

    /*
    @RequestMapping(path = "/recommend/event/byFriends", method = RequestMethod.GET)
    public ResponseEntity getEventByFriends(@RequestParam(value = "token") String token,
                                            @RequestParam(value = "latitude") String latitude,
                                            @RequestParam(value = "longitude") String longitude) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        try {
            List<GroupInfo> result = recommendationService.recommendEventByFriends(Float.parseFloat(latitude),
                    Float.parseFloat(longitude), actor);

            return ResponseEntity.ok(result);
        } catch (GoogleException | VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    */

    @RequestMapping(path = "/recommend/friends", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getFriendsByCheckins(@RequestParam(value = "token") String token) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(token);
            if (actor == null) {
                defResult.setResult(ResponseEntity.ok(new ErrorResponse("User not authenticated")));
                return;
            }

            try {
                List<UserInfo> result = recommendationService.recommendFriends(actor);
                defResult.setResult(ResponseEntity.ok(result));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }

    @RequestMapping(path = "/recommend/groups", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getGroupsByCheckins(@RequestParam(value = "token") String token) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(token);
            if (actor == null) {
                defResult.setResult(ResponseEntity.ok(new ErrorResponse("User not authenticated")));
                return;
            }

            try {
                List<GroupInfo> groups = recommendationService.recommendGroups(actor);
                defResult.setResult(ResponseEntity.ok(groups));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }

}
