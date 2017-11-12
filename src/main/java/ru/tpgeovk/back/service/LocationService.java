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

import java.util.ArrayList;
import java.util.List;
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
}
