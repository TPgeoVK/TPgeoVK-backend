package ru.tpgeovk.back.ru.tpgeovk.back.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TokenService  {

    private HashMap<Integer, String> tokens = new HashMap<>();

    public void putToken(Integer userId, String token) {
        tokens.put(userId, token);
    }

    public String getToken(Integer userId) {
        if (!tokens.containsKey(userId)) {
            return null;
        }
        String token = tokens.get(userId);
        if (token == null) {
            tokens.remove(userId);
        }
        return token;
    }
}
