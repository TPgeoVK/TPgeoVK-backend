package ru.tpgeovk.back.ru.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TokenService  {

    private HashMap<String, Integer> tokenToUser = new HashMap<>();

    public void put(String token, Integer userId) {
        tokenToUser.put(token, userId);
    }

    public Integer getUserId(String token) {
        if (!tokenToUser.containsKey(token)) {
            return null;
        }

        Integer userId = tokenToUser.get(token);
        if (userId == null) {
            tokenToUser.remove(token);
        }

        return userId;
    }
}
