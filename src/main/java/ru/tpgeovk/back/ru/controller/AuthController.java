package ru.tpgeovk.back.ru.controller;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.tpgeovk.back.VkContext;
import ru.tpgeovk.back.ru.model.response.ErrorResponse;
import ru.tpgeovk.back.ru.service.TokenService;

@Controller
public class AuthController {

    private static final String ENV_DEPLOY_URL = "TPGEOVK_DEPLOY_URL";

    private static final String SERVER_URL;

    private static final String CODE_REDIRECT_URI;
    private static final String OAUTH_REDIRECT_URI;

    private static final String REDIRECT_SUCCESS;
    private static final String REDIRECT_ERROR;

    static {
        SERVER_URL = System.getenv(ENV_DEPLOY_URL);
        if (SERVER_URL == null) {
            throw new RuntimeException("Environment variable TPGEOVK_DEPLOY_URL is wrong or not set");
        }

        CODE_REDIRECT_URI = SERVER_URL.concat("/auth/callback");
        OAUTH_REDIRECT_URI = "https://oauth.vk.com/authorize?" +
                "client_id=" + VkContext.getAppId() +
                "&redirect_uri=" + CODE_REDIRECT_URI +
                "&display=mobile" +
                "&scope=" + VkContext.getScope() +
                "&response_type=code" +
                "&v=" + VkContext.getApiVersion();

        REDIRECT_SUCCESS = SERVER_URL.concat("/auth/callback/success");
        REDIRECT_ERROR = SERVER_URL.concat("/auth/callback/error");
    }

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
    public ModelAndView authCodeCallback(@RequestParam(value = "code") String code) {
        UserAuthResponse response = null;

        try {
            response = vk.oauth().userAuthorizationCodeFlow(VkContext.getAppId(),
                    VkContext.getSecureKey(), CODE_REDIRECT_URI, code)
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();;
            return new ModelAndView("redirect:" + REDIRECT_ERROR + "?message=" + e.getMessage());
        }

        Integer usereId = response.getUserId();
        String token = response.getAccessToken();

        tokenService.put(token, usereId);

        return new ModelAndView("redirect:" + REDIRECT_SUCCESS + "?userid=" + usereId.toString() +
            "&token=" + token);
    }

    @RequestMapping(path = "/auth/callback/success", method = RequestMethod.GET)
    public ResponseEntity authSuccess(@RequestParam(value = "userid") String userId,
                                      @RequestParam(value = "token") String token) {
        /** Метод нужен только для задания URL'а с токеном и id пользователя */
        return ResponseEntity.ok(null);
    }

    @RequestMapping(path = "/auth/callback/error", method = RequestMethod.GET)
    public ResponseEntity authError(@RequestParam(value = "message") String message) {
        /** Метод нужен только для задания URL'а с сообщением об ошибке */
        return ResponseEntity.ok(null);
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
