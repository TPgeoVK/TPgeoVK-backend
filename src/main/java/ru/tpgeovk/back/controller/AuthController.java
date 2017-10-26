package ru.tpgeovk.back.controller;

import com.google.gson.*;
import com.vk.api.sdk.client.ClientResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.request.TokenRequest;
import ru.tpgeovk.back.model.response.ErrorResponse;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.TokenService;
import ru.tpgeovk.back.service.UsersDataService;
import ru.tpgeovk.back.service.VkProxyService;

import java.io.IOException;
import java.util.List;

@Controller
public class AuthController {

    private static final String OAUTH_REDIRECT_URI;

    static {

        OAUTH_REDIRECT_URI = "https://oauth.vk.com/authorize?" +
                "client_id=" + VkContext.getAppId() +
                "&redirect_uri=https://oauth.vk.com/blank.html" +
                "&display=mobile" +
                "&scope=" + VkContext.getScope() +
                "&response_type=token" +
                "&v=" + VkContext.getApiVersion();

    }

    private final TokenService tokenService;

    private final VkApiClient vk = VkContext.getVkApiClient();

    private final OkHttpClient httpClient;
    private final Gson gson;


    @Autowired
    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
        this.httpClient = new OkHttpClient();
        gson = new GsonBuilder().create();
    }

    @RequestMapping(path = "/auth", method = RequestMethod.GET)
    public ModelAndView authRedirect() {
        return new ModelAndView("redirect:" + OAUTH_REDIRECT_URI);
    }

    @RequestMapping(path = "/auth/login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody TokenRequest request) {
        Integer userId;
        String token = request.getToken();
        if (StringUtils.isEmpty(token)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Empty token"));
        }
        try {
            userId = resolveUser(token);
            if (userId.equals(0)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unable to resolve user"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        tokenService.put(token, userId);

        return ResponseEntity.ok(null);
    }

    @RequestMapping(path = "/auth/logout", method = RequestMethod.POST)
    public ResponseEntity logout(@RequestBody TokenRequest request) {
        if (StringUtils.isEmpty(request.getToken())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Empty token"));
        }
        tokenService.remove(request.getToken());
        return ResponseEntity.ok(null);
    }


    private int resolveUser(String token) throws IOException {
        String url = "https://api.vk.com/method/users.get?access_token=" + token + "&v=" + VkContext.getApiVersion();
        String response = "";
        boolean ok = false;
        while (!ok) {
            Request request = new Request.Builder().url(url).get().build();
            Response clientResponse = httpClient.newCall(request).execute();
            response = clientResponse.body().string();
            if (!response.contains("many")) {
                ok = true;
            }
        }
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
        if ((jsonObject.getAsJsonArray("response") == null) ||
                (jsonObject.getAsJsonArray("response").size() == 0)) {
            throw new IOException(response);

        }

        return jsonObject.getAsJsonArray("response").get(0)
                .getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
    }
}
