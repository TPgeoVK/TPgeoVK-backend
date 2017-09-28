package ru.tpgeovk.back.ru.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpgeovk.back.ru.model.GroupInfo;
import ru.tpgeovk.back.ru.model.response.ErrorResponse;
import ru.tpgeovk.back.ru.service.RecommendationService;
import ru.tpgeovk.back.ru.service.TokenService;

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
    public ResponseEntity getEventByFriends(@RequestParam(value = "userid") Integer userId,
                                            @RequestParam(value = "country") String country,
                                            @RequestParam(value = "city") String city) {

        String token = tokenService.getToken(userId);
        if (token == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        UserActor actor = new UserActor(userId, token);
        List<GroupInfo> result = recommendationService.recommendEventByFriends(city, country, actor);
        if (result == null) {
            return ResponseEntity.ok(new ErrorResponse("Unable to recommend any events"));
        }

        return ResponseEntity.ok(result);
    }
}