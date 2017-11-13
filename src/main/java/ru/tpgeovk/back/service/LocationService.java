package ru.tpgeovk.back.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.places.PlaceFull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.model.deserializer.UserFeaturesDeserializer;
import ru.tpgeovk.back.scripts.VkScripts;

import java.util.*;
import java.util.stream.Collectors;

import static ru.tpgeovk.back.scripts.VkScripts.PLACE_CHECKINS_USERS;

@Service
public class LocationService {

    private final VkApiClient vk;

    private final Gson gson;

    @Autowired
    public LocationService() {
        vk = VkContext.getVkApiClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(UserFeatures.class, new UserFeaturesDeserializer())
                .create();
    }

    public FullPlaceInfo detectPlace(UserActor actor, Float latitude, Float longitude) throws VkException {
        List<FullPlaceInfo> nearestPlaces = getNearestPlaces(actor, latitude, longitude);
        return detectPlace(actor, nearestPlaces);
    }

    public FullPlaceInfo detectPlace(UserActor actor, List<FullPlaceInfo> places) throws VkException {
        Map<FullPlaceInfo, Float> placeRatings = new HashMap<>();

        UserFeatures actorFeatures = getActorFeatures(actor);

        for (FullPlaceInfo place : places) {
            List<UserFeatures> users = getUsersFromPlace(actor, place.getId());
            /** Встречаются случаи, когда в данном месте есть чекины, но метод getCheckins ничего не возвращает */
            if (users.isEmpty()) {
                continue;
            }
            float usersCount = (float)users.size();

            final Boolean actorGender = actorFeatures.getGender();
            long actorGenderCount = users.stream()
                    .filter(a -> actorGender.equals(a.getGender()))
                    .count();
            Float genderPercent = (float)actorGenderCount / usersCount;

            Float agePercent = 0f;
            final Integer actorAge = actorFeatures.getAge();
            if (actorAge != null) {
                long actorAgeCount = users.stream()
                        .filter(a -> (a.getAge() != null) && isAgeSimilar(actorAge, a.getAge()))
                        .count();
                agePercent = (float)actorAgeCount / usersCount;
            }

            Float commonGroupsSum = 0f;
            for (UserFeatures user : users) {
                commonGroupsSum  = commonGroupsSum +
                        calculateGroupsSimilarity(actorFeatures.getGroups(), user.getGroups());
            }
            Float groupsPercent = commonGroupsSum / usersCount;

            /**TODO: изменить функцию нормализации */
            Float checkinsRating = 1f - (1f / (float)place.getCheckinsCount());
            Float distanceRating = 1f - ((float)place.getDistance() / 2400f);

            /** TODO: подобрать коэффициенты */
            Float rating = 0.25f*genderPercent + 0.25f*agePercent + groupsPercent + checkinsRating + distanceRating;

            placeRatings.put(place, rating);
        }

        return placeRatings.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    public List<FullPlaceInfo> getNearestPlaces(UserActor actor, Float latitude, Float longitude)
            throws VkException {
        List<PlaceFull> nearestPlaces = null;
        boolean ok = false;
        while (!ok) {
            try {
                nearestPlaces = vk.places().search(actor, latitude, longitude).q("*")
                        .radius(1)
                        .execute()
                        .getItems();
                if (nearestPlaces.size() == 0) {
                    nearestPlaces = vk.places().search(actor, latitude, longitude)
                            .q("*")
                            .radius(2)
                            .execute()
                            .getItems();
                }
                ok = true;
            } catch (ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(50);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }
        }
        if (nearestPlaces.size() == 0) {
            return new ArrayList<>();
        }

        return nearestPlaces.stream()
                .map(a -> FullPlaceInfo.fromPlaceFull(a))
                .collect(Collectors.toList());
    }

    public List<UserFeatures> getUsersFromPlace(UserActor actor, Integer placeId) throws VkException {
        JsonElement response = null;
        boolean ok = false;
        String script = String.format(VkScripts.PLACE_CHECKINS_USERS, placeId);
        ok = false;
        while (!ok) {
            try {
                response = vk.execute().code(actor, script).execute();
                ok = true;
            } catch (ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(100);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                e.printStackTrace();
                throw new VkException(e.getMessage(), e);
            }
        }
        if (!response.isJsonArray()) {
            return new ArrayList<>();
        }
        List<Integer> userIds = gson.fromJson(response, new TypeToken<List<Integer>>(){}.getType());

        List<UserFeatures> result = new ArrayList<>();
        for (Integer userId : userIds) {
            script = String.format(VkScripts.GET_USER_FEATURES, userId);
            ok = false;
            while (!ok) {
                try {
                    response = vk.execute().code(actor, script).execute();
                    ok = true;
                } catch (ApiException | ClientException e) {
                    if (e instanceof ApiTooManyException) {
                        try {
                            Thread.currentThread().sleep(100);
                            continue;
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    e.printStackTrace();
                    throw new VkException(e.getMessage(), e);
                }
            }

            UserFeatures user = gson.fromJson(response, UserFeatures.class);
            result.add(user);
        }

        return result;
    }

    private UserFeatures getActorFeatures(UserActor actor) throws VkException {
        JsonElement response = null;
        String script = String.format(VkScripts.GET_USER_FEATURES, actor.getId());
        boolean ok = false;
        while (!ok) {
            try {
                response = vk.execute().code(actor, script).execute();
                ok = true;
            } catch (ApiException | ClientException e) {
                if (e instanceof ApiTooManyException) {
                    try {
                        Thread.currentThread().sleep(100);
                        continue;
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                e.printStackTrace();
                throw new VkException(e.getMessage(), e);
            }
        }

        UserFeatures actorFeatures = gson.fromJson(response, UserFeatures.class);
        return actorFeatures;
    }

    private Boolean isAgeSimilar(Integer actorAge, Integer comparingAge) {
        Integer youngerRadius = 0;
        Integer olderRadius = 0;

        if (actorAge < 12) {
            youngerRadius = 12;
            olderRadius = 5;
        }
        else if ((actorAge >= 12) && (actorAge < 17)) {
            youngerRadius = 4;
            olderRadius = 4;
        }
        else if ((actorAge >= 17) && (actorAge < 25)) {
            youngerRadius = 3;
            olderRadius = 7;
        }
        else if ((actorAge >= 25) && (actorAge < 31)) {
            youngerRadius = 5;
            olderRadius = 8;
        }
        else if ((actorAge >= 31) && (actorAge < 40)) {
            youngerRadius = 7;
            olderRadius = 12;
        }
        else {
            youngerRadius = 10;
            olderRadius = 40;
        }

        if (actorAge >= comparingAge) {
            return  actorAge - comparingAge <= youngerRadius;
        } else {
            return comparingAge - actorAge <= olderRadius;
        }
    }

    private Integer countCommonGroups(List<String> actorGroups, List<String> comparingGroups) {
        List<Integer> comparingCopy = new ArrayList(comparingGroups);
        comparingCopy.retainAll(actorGroups);
        return comparingCopy.size();
    }

    private Float calculateGroupsSimilarity(List<String> actorGroups, List<String> comparingGroups) {
        Integer commonGroups = countCommonGroups(actorGroups, comparingGroups);
        Float result = (float)commonGroups / ((float)(actorGroups.size() + comparingGroups.size() - commonGroups));
        return result;
    }
}
