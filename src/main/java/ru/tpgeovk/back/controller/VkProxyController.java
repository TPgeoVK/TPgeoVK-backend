package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserInfo;
import ru.tpgeovk.back.model.request.CreatePostRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.VkProxyService;

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
    public DeferredResult<ResponseEntity> getUser(@RequestParam(value = "token") String token) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(token);
            if (actor == null) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received")));
                return;
            }

            UserInfo result;
            try {
                result = vkService.getUser(actor);
                defResult.setResult(ResponseEntity.ok(result));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }

    @RequestMapping(path = "/vkapi/checkins/all", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getAllCheckins(@RequestParam(value = "token") String token) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(token);
            if (actor == null) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received")));
                return;
            }

            List<CheckinInfo> result;
            try {
                result = vkService.getAllUserCheckins(actor);
                defResult.setResult(ResponseEntity.ok(result));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }

    @RequestMapping(path = "/vkapi/checkins/latest", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getLatestCheckins(@RequestParam(value = "token") String token,
                                            @RequestParam(value = "latitude") String lat,
                                            @RequestParam(value = "longitude") String lon) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            Float latitude;
            Float longitude;
            try {
                latitude = Float.parseFloat(lat);
                longitude = Float.parseFloat(lon);
            } catch (NumberFormatException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse("Wrong coordinates values!")));
                return;
            }

            UserActor actor = tokenService.getUser(token);
            if (actor == null) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received")));
                return;
            }

            List<CheckinInfo> result;
            try {
                result = vkService.getLatestCheckins(actor, latitude, longitude);
                defResult.setResult(ResponseEntity.ok(result));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }

    @RequestMapping(path = "/vkapi/post/create", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> createPost(@RequestBody CreatePostRequest request) {
        DeferredResult<ResponseEntity> defResult = new DeferredResult<>();

        new Thread(() -> {
            UserActor actor = tokenService.getUser(request.getToken());
            if (actor == null) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse("Unknown token received")));
                return;
            }

            try {
                CheckinInfo result = vkService.createPost(actor, request.getPlaceId(), request.getText());
                defResult.setResult(ResponseEntity.ok(result));
            } catch (VkException e) {
                defResult.setResult(ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage())));
            }
        }).start();

        return defResult;
    }
}
