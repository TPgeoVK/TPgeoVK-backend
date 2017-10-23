package ru.tpgeovk.back.service;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TokenService  {

    private HashMap<String, UserActor> tokenToUser = new HashMap<>();

    public void put(String token, Integer userId) {
        tokenToUser.put(token, new UserActor(userId, token));
    }

    public UserActor getUser(String token) {
        if (!tokenToUser.containsKey(token)) {
            return null;
        }

        UserActor user = tokenToUser.get(token);
        if (user == null) {
            tokenToUser.remove(token);
        }

        return user;
    }
}
