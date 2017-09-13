package ru.tpgeovk.back.ru.tpgeovk.back.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.tpgeovk.back.VkContext;
import ru.tpgeovk.back.ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.ru.tpgeovk.back.service.TokenService;

import javax.validation.Path;

@Controller
public class AuthController {

    private static final String CODE_REDIRECT_URI = "http://localhost:8080/auth/callback";

    private static final String OAUTH_REDIRECT_URI = "https://oauth.vk.com/authorize?" +
            "client_id=" + VkContext.getAppId() +
            "&redirect_uri=" + CODE_REDIRECT_URI +
            "&display=mobile" +
            "&scope=friends,pages,notes,wall,groups" +
            "&response_type=code" +
            "&v=5.68";

    private final TokenService tokenService;

    private final VkApiClient vk = VkContext.getVkApiClient();

    @Autowired
    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @RequestMapping(path = "/auth", method = RequestMethod.GET)
    public ModelAndView authRedirect() {
        return new ModelAndView("redirect:" + OAUTH_REDIRECT_URI);
    }

    @RequestMapping(path = "/auth/callback", method = RequestMethod.GET)
    public ResponseEntity authCodeCallback(@RequestParam(value = "code") String code) {
        try {
            UserAuthResponse response = vk.oauth().userAuthorizationCodeFlow(VkContext.getAppId(),
                    VkContext.getSecureKey(), CODE_REDIRECT_URI, code)
                    .execute();

            Integer userId = response.getUserId();
            String token = response.getAccessToken();

            tokenService.putToken(userId, token);
            System.out.println(userId.toString() + " " + token);

            return ResponseEntity.ok(new UserIdResponse(userId));

        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ErrorResponse(e.getMessage()));
        }
    }

    private class UserIdResponse {
        private Integer userId;

        public UserIdResponse(Integer userId) {
            this.userId = userId;
        }

        public Integer getUserId() {
            return userId;
        }
    }
}
