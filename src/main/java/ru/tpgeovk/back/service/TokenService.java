package ru.tpgeovk.back.service;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService  {

    private Map<String, Integer> tokenToUser = new ConcurrentHashMap<>();

    public void put(String token, Integer userId) {
        tokenToUser.put(token, userId);
    }

    public UserActor getUser(String token) {
        if (!tokenToUser.containsKey(token)) {
            return null;
        }

        String copiedToken = String.valueOf(token);
        Integer originalId = tokenToUser.get(token);
        Integer copiedUserId = Integer.valueOf(tokenToUser.get(token));

        return new UserActor(copiedUserId, copiedToken);
    }

    public void remove(String  token) {
        tokenToUser.remove(token);
    }
}
