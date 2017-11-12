package ru.tpgeovk.back.service.background;

import com.vk.api.sdk.client.actors.UserActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.CheckinInfo;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.UserInfo;
import ru.tpgeovk.back.service.RecommendationService;
import ru.tpgeovk.back.service.UsersDataService;
import ru.tpgeovk.back.service.VkProxyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BackgroundService {

    private final UsersDataService usersDataService;
    private final VkProxyService vkProxyService;
    private final RecommendationService recommendationService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Map<String, TaskStatus> tasks = new HashMap<>();

    @Autowired
    public BackgroundService(UsersDataService usersDataService,
                             VkProxyService vkProxyService,
                             RecommendationService recommendationService) {
        this.usersDataService = usersDataService;
        this.vkProxyService = vkProxyService;
        this.recommendationService = recommendationService;
    }

    public void trigger(UserActor actor, Float latitude, Float longitude) {
        TaskStatus currentTask = tasks.get(actor.getAccessToken());
        if (isTaskActive(currentTask)) {
            return;
        }
        tasks.put(actor.getAccessToken(), new TaskStatus());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String token = actor.getAccessToken();
                TaskStatus task = tasks.get(token);
                if (task == null) {
                    return;
                }
                usersDataService.createForUser(token);

                try {
                    List<CheckinInfo> userCheckins = vkProxyService.getAllUserCheckins(actor);
                    usersDataService.getCheckins(token).clear();
                    usersDataService.getCheckins(token).addAll(userCheckins);

                    List<Integer> users = recommendationService.getUsersFromCheckins(actor, userCheckins);
                    Map<Integer, List<Integer>> usersGroups = recommendationService.getSimilarUsers(actor, users);
                    List<UserInfo> friends= recommendationService.getSimilarUsersInfo(actor, usersGroups);
                    usersDataService.getRecommendedFriends(token).clear();
                    usersDataService.getRecommendedFriends(token).addAll(friends);
                    task.setFriendsCompleted(true);

                    List<FullPlaceInfo> places = recommendationService.recommendNearestPlaces(actor, latitude, longitude);
                    usersDataService.getRecommendedNearestPlaces(token).clear();
                    usersDataService.getRecommendedNearestPlaces(token).addAll(places);
                    task.setPlacesCompleted(true);

                } catch (VkException e) {
                    tasks.put(token, new FailedTask(task.getFriendsCompleted(), task.getPlacesCompleted(),
                            e.getMessage()));
                }
            }
        });
    }

    public TaskStatus getStatus(String token) {
        TaskStatus status = tasks.get(token);
        if (status == null) {
            return null;
        }
        if (status instanceof FailedTask) {
            FailedTask error = (FailedTask)status;
            return new FailedTask(error.getFriendsCompleted(), error.getPlacesCompleted(), error.getError());
        }
        return new TaskStatus(status.getFriendsCompleted(), status.getPlacesCompleted());
    }

    private boolean isTaskActive(TaskStatus task) {
        if (task == null) {
            return false;
        }

        if (task instanceof FailedTask) {
            return false;
        }
        boolean friends = task.getFriendsCompleted();
        boolean places = task.getPlacesCompleted();
        return friends && places;
    }
}
