package ru.tpgeovk.back.service;

import org.springframework.stereotype.Service;
import ru.tpgeovk.back.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsersDataService {

    private Map<String, List<CheckinInfo>> usersCheckins = new HashMap<>();

    private Map<String, List<UserInfo>> recommendedFriends = new HashMap<>();
    private Map<String, List<FullPlaceInfo>> recommendedNearestPlaces = new HashMap<>();
    private Map<String, List<GroupInfo>> recommendedGroups = new HashMap<>();

    public List<CheckinInfo> getCheckins(String token) {
        return usersCheckins.get(token);
    }

    public List<UserInfo> getRecommendedFriends(String token) {
        return recommendedFriends.get(token);
    }

    public List<FullPlaceInfo> getRecommendedNearestPlaces(String token) {
        return recommendedNearestPlaces.get(token);
    }

    public List<GroupInfo> getRecommendedGroups(String token) {
        return recommendedGroups.get(token);
    }

    public void createForUser(String token) {
        if (usersCheckins.get(token) != null) {
            return;
        }

        usersCheckins.put(token, new ArrayList<>());
        recommendedFriends.put(token, new ArrayList<>());
        recommendedGroups.put(token, new ArrayList<>());
        recommendedNearestPlaces.put(token, new ArrayList<>());
    }

    public void removeForUser(String token) {
        usersCheckins.remove(token);
        recommendedFriends.remove(token);
        recommendedNearestPlaces.remove(token);
        recommendedGroups.remove(token);
    }
}
