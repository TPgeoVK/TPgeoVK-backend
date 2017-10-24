package ru.tpgeovk.back.controller;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.background.BackgroundService;
import ru.tpgeovk.back.service.background.TaskStatus;

@RestController
public class TriggerController {

    private final TokenService tokenService;
    private final BackgroundService backgroundService;

    @Autowired
    public TriggerController(TokenService tokenService,
                             BackgroundService backgroundService) {
        this.tokenService = tokenService;
        this.backgroundService = backgroundService;
    }

    @RequestMapping(path = "/trigger", method = RequestMethod.GET)
    public ResponseEntity postTrigger(@RequestParam(value = "token") String token,
                                      @RequestParam(value = "latitude") String latitude,
                                      @RequestParam(value = "longitude") String longitude) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.ok(new ErrorResponse("User not authenticated"));
        }

        Float lat;
        Float lon;
        try {
            lat = Float.parseFloat(latitude);
            lon = Float.parseFloat(longitude);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Wrong coodrdinates format"));
        }

        backgroundService.trigger(actor, lat, lon);

        return ResponseEntity.ok(null);
    }

    @RequestMapping(path =  "/trigger/status", method = RequestMethod.GET)
    public ResponseEntity getStatus(@RequestParam(value = "token") String token) {

        UserActor actor = tokenService.getUser(token);
        if (actor == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("User not authenticated"));
        }

        TaskStatus task = backgroundService.getStatus(actor.getAccessToken());
        if (task == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("No triggered task"));
        }

        return ResponseEntity.ok(task);
    }
}
