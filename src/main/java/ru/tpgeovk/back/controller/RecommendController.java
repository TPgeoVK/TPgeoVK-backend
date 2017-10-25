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
import ru.tpgeovk.back.model.*;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.UsersDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class RecommendController {

    private final TokenService tokenService;
    private final RecommendationService recommendationService;
    private final UsersDataService usersDataService;

    @Autowired
    public RecommendController(TokenService tokenService,
                               RecommendationService recommendationService,
                               UsersDataService usersDataService) {
        this.tokenService = tokenService;
        this.recommendationService = recommendationService;
        this.usersDataService = usersDataService;
    }

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

    @RequestMapping(path = "/recommend/friends", method = RequestMethod.GET)
    public ResponseEntity getFriendsByCheckins(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        List<CheckinInfo> userCheckins = new ArrayList<>(usersDataService.getCheckins(token));

        try {
            List<Integer> users = recommendationService.getUsersFromCheckins(actor, userCheckins);
            Map<Integer, List<Integer>> usersGroups = recommendationService.getSimilarUsers(actor, users);
            List<UserInfo> friends= recommendationService.getSimilarUsersInfo(actor, usersGroups);

            return ResponseEntity.ok(friends);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @RequestMapping(path = "/recommend/groups", method = RequestMethod.GET)
    public ResponseEntity getGroupsByCheckins(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        List<CheckinInfo> userCheckins = new ArrayList<>(usersDataService.getCheckins(token));

        try {
            List<GroupInfo> groups = recommendationService.recommendGroupsByCheckins(actor, userCheckins);
            return ResponseEntity.ok(groups);
        } catch (VkException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @RequestMapping(path = "/recommend/places/nearest", method = RequestMethod.GET)
    public ResponseEntity getPlaceByCheckins(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        return ResponseEntity.ok(usersDataService.getRecommendedNearestPlaces(token));
    }
}
