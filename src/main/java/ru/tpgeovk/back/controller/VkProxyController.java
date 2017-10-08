package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
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

    @RequestMapping(path = "/vkapi/checkins/all", method = RequestMethod.GET)
    public ResponseEntity getAllCheckins(@RequestParam(value = "token") String token) {

        Integer userId = tokenService.getUserId(token);
        if (userId == null) {
            return ResponseEntity.ok(new ErrorResponse("Unknown token received"));
        }
        UserActor actor = new UserActor(userId, token);

        List<CheckinInfo> result;
        try {
            result = vkService.getAllUserCheck(actor);
        } catch (VkException e) {
            return ResponseEntity.ok(new ErrorResponse(e.getMessage()));
        }

        return ResponseEntity.ok(result);
    }
}
