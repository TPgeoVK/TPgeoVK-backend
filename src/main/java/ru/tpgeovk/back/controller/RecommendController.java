package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpgeovk.back.exception.GoogleException;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.GroupInfo;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.TokenService;

import java.util.List;

@RestController
public class RecommendController {

    private final TokenService tokenService;
    private final RecommendationService recommendationService;

    @Autowired
    public RecommendController(TokenService tokenService, RecommendationService recommendationService) {
        this.tokenService = tokenService;
        this.recommendationService = recommendationService;
    }

    @RequestMapping(path = "/recommend/event/byFriends", method = RequestMethod.GET)
    public ResponseEntity getEventByFriends(@RequestParam(value = "token") String token,
                                            @RequestParam(value = "latitude") String latitude,
                                            @RequestParam(value = "longitude") String longitude) {

        Integer userId = tokenService.getUserId(token);
        if (token == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }
        UserActor actor = new UserActor(userId, token);

        try {
            List<GroupInfo> result = recommendationService.recommendEventByFriends(Float.parseFloat(latitude),
                    Float.parseFloat(longitude), actor);

            return ResponseEntity.ok(result);
        } catch (GoogleException | VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @RequestMapping(path = "/recommend/groups/byCheckins", method = RequestMethod.GET)
    public ResponseEntity getGroupByCheckins(@RequestParam(value = "token") String token) {

        Integer userId = tokenService.getUserId(token);
        if (token == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }
        UserActor actor = new UserActor(userId, token);

        try {
            List<Integer> result = recommendationService.getUsersFromCheckins(actor);
            return ResponseEntity.ok(result);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
