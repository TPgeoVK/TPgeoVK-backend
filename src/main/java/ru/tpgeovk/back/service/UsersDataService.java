package ru.tpgeovk.back.service;

import org.springframework.stereotype.Service;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.GroupInfo;
import ru.tpgeovk.back.model.PlaceInfo;
import ru.tpgeovk.back.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsersDataService {

    private Map<String, UserInfo> usersInfo = new HashMap<>();
    private Map<String, List<CheckinInfo>> usersCheckins = new HashMap<>();

    private Map<String, List<UserInfo>> recommendedFriends = new HashMap<>();
    private Map<String, List<PlaceInfo>> recommendedNearestPlaces = new HashMap<>();
    private Map<String, List<GroupInfo>> recommendedGroups = new HashMap<>();

    public UserInfo getUserInfo(String token) {
        return usersInfo.get(token);
    }

    public void setUsersInfo(String token, UserInfo userInfo) {
        usersInfo.put(token, userInfo);
    }

    public List<CheckinInfo> getCheckins(String token) {
        return usersCheckins.get(token);
    }

    public List<UserInfo> getRecommendedFriends(String token) {
        return recommendedFriends.get(token);
    }

    public List<PlaceInfo> getRecommendedNearestPlaces(String token) {
        return recommendedNearestPlaces.get(token);
    }

    public List<GroupInfo> getRecommendedGroups(String token) {
        return recommendedGroups.get(token);
    }

    public void removeAll(String token) {
        usersInfo.remove(token);
        usersCheckins.remove(token);
        recommendedFriends.remove(token);
        recommendedNearestPlaces.remove(token);
        recommendedGroups.remove(token);
    }
}
