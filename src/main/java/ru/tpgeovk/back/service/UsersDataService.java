package ru.tpgeovk.back.service;

import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersDataService {

    private Map<String, UserInfo> usersInfo = new HashMap<>();
    private Map<String, List<CheckinInfo>> usersCheckins = new HashMap<>();

    private Map<String, List<UserInfo>> recommendedFriends = new HashMap<>();

}
