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
import org.springframework.util.StringUtils;
import ru.tpgeovk.back.contexts.GsonContext;
import ru.tpgeovk.back.contexts.VkContext;
import ru.tpgeovk.back.exception.VkException;
import ru.tpgeovk.back.model.FullPlaceInfo;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.model.deserializer.UserFeaturesDeserializer;
import ru.tpgeovk.back.model.features.PlaceFeatures;
import ru.tpgeovk.back.scripts.VkScripts;
import ru.tpgeovk.back.text.TextProcessor;
import ru.tpgeovk.back.util.ModelUtil;

import java.util.*;
import java.util.stream.Collectors;

import static ru.tpgeovk.back.scripts.VkScripts.PLACE_CHECKINS_USERS;

@Service
public class LocationService {

    private final VkApiClient vk;

    private final Gson gson;

    private final VkProxyService vkService;

    @Autowired
    public LocationService(VkProxyService vkService) {
        vk = VkContext.getVkApiClient();
        gson = GsonContext.createGson();
        this.vkService = vkService;
    }

    public FullPlaceInfo detectPlace(UserActor actor, Float latitude, Float longitude, String post)
            throws VkException {
        List<FullPlaceInfo> nearestPlaces = getNearestPlaces(actor, latitude, longitude);
        return detectPlace(actor, nearestPlaces, post);
    }

    public FullPlaceInfo detectPlace(UserActor actor, List<FullPlaceInfo> places, String post)
            throws VkException {
        Map<FullPlaceInfo, Float> placeRatings = new HashMap<>();

        UserFeatures actorFeatures = vkService.getActorFeatures(actor);

        List<PlaceFeatures> placesFeatures = new ArrayList<>();
        for (FullPlaceInfo place : places) {
            List<UserFeatures> users = getUsersFromPlace(actor, place.getId());
            /** Встречаются случаи, когда в данном месте есть чекины, но метод getCheckins ничего не возвращает */
            if (users.isEmpty()) {
                continue;
            }

            PlaceFeatures placeFeatures = new PlaceFeatures();
            placeFeatures.setPlaceId(place.getId());
            placeFeatures.setCheckinsCount(place.getCheckinsCount());
            placeFeatures.setDistance(2800 - place.getDistance());

            float usersCount = (float)users.size();

            Float genderPercent = 0f;
            if (place.getCheckinsCount() > 300) {
                final Boolean actorGender = actorFeatures.getGender();
                long actorGenderCount = users.stream()
                        .filter(a -> actorGender.equals(a.getGender()))
                        .count();
                genderPercent = (float) actorGenderCount / usersCount;
            }
            placeFeatures.setSameGenderPercent(genderPercent);

            Float agePercent = 0f;
            if (place.getCheckinsCount() > 300) {
                final Integer actorAge = actorFeatures.getAge();
                if (actorAge != null) {
                    long actorAgeCount = users.stream()
                            .filter(a -> (a.getAge() != null) && ModelUtil.isAgeSimilar(actorAge, a.getAge()))
                            .count();
                    agePercent = (float) actorAgeCount / usersCount;
                }
            }
            placeFeatures.setSameAgePercent(agePercent);

            Float groupsSimilaritySum = 0f;
            Integer similarityCount = 0;
            for (UserFeatures user : users) {
                Float similarity = calculateGroupsSimilarity(actorFeatures.getGroups(), user.getGroups());
                if (!similarity.equals(0f)) {
                    groupsSimilaritySum = groupsSimilaritySum + similarity;
                    similarityCount = similarityCount + 1;
                }
            }
            if (!groupsSimilaritySum.equals(0f)) {
                Float groupsPercent = groupsSimilaritySum / (float)similarityCount;
                placeFeatures.setGroupsSimilarity(groupsPercent);
            } else {
                placeFeatures.setGroupsSimilarity(0f);
            }

            Float textSimilarity = 0f;
            if (!StringUtils.isEmpty(post)) {
                textSimilarity = TextProcessor.compareTextsSimple(place.getTitle(), post);
            }
            placeFeatures.setTextSimilarity(textSimilarity);

            placesFeatures.add(placeFeatures);
        }


        Integer checkinsMin = places.stream()
                .mapToInt(FullPlaceInfo::getCheckinsCount)
                .min()
                .getAsInt();
        Integer checkinsMax = places.stream()
                .mapToInt(FullPlaceInfo::getCheckinsCount)
                .max()
                .getAsInt();
        if (checkinsMax.equals(0)) {
            checkinsMax = 1;
        }

        Float groupsSimilarityMin = (float)placesFeatures.stream()
                .mapToDouble(PlaceFeatures::getGroupsSimilarity)
                .min()
                .getAsDouble();
        Float groupsSimilarityMax = (float)placesFeatures.stream()
                .mapToDouble(PlaceFeatures::getGroupsSimilarity)
                .max()
                .getAsDouble();
        if (groupsSimilarityMax.equals(0f)) {
            groupsSimilarityMax = 1f;
        }

        Float textSimilarityMin = (float)placesFeatures.stream()
                .mapToDouble(PlaceFeatures::getTextSimilarity)
                .min()
                .getAsDouble();
        Float textSimilarityMax = (float)placesFeatures.stream()
                .mapToDouble(PlaceFeatures::getTextSimilarity)
                .max()
                .getAsDouble();
        if (textSimilarityMax.equals(0f)) {
            textSimilarityMax = 1f;
        }

        Integer distanceMin = placesFeatures.stream()
                .mapToInt(PlaceFeatures::getDistance)
                .min()
                .getAsInt();
        Integer distanceMax = placesFeatures.stream()
                .mapToInt(PlaceFeatures::getDistance)
                .max()
                .getAsInt();
        if (distanceMax.equals(distanceMin)) {
            distanceMin = 0;
        }

        for (PlaceFeatures placeFeatures : placesFeatures) {
            Float checkinsRating = (float)(placeFeatures.getCheckinsCount() - checkinsMin) /
                    (float)(checkinsMax - checkinsMin);
            Float groupsSimilarityRating = (placeFeatures.getGroupsSimilarity() - groupsSimilarityMin) /
                    (groupsSimilarityMax - groupsSimilarityMin);
            Float textSimilarityRating = (placeFeatures.getTextSimilarity() - textSimilarityMin) /
                    (textSimilarityMax - textSimilarityMin);
            Float distanceRating = (float)(placeFeatures.getDistance() - distanceMin) /
                    (float)(distanceMax - distanceMin);
            Float ageRating = placeFeatures.getSameAgePercent();
            Float genderRating = placeFeatures.getSameGenderPercent();

            Float placeRating = checkinsRating + 1.2f*groupsSimilarityRating + 1.7f*distanceRating + 0.25f*ageRating +
                    0.25f*genderRating + 10f*textSimilarityRating;

            FullPlaceInfo placeInfo = places.stream()
                    .filter(a -> a.getId().equals(placeFeatures.getPlaceId()))
                    .findFirst()
                    .get();
            placeRatings.put(placeInfo, placeRating);
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
        userIds = userIds.stream()
                .distinct()
                .collect(Collectors.toList());

        return vkService.getUsersFeatures(actor, userIds);
    }

    private Float calculateGroupsSimilarity(List<String> actorGroups, List<String> comparingGroups) {
        Integer commonGroups = ModelUtil.countCommonGroups(actorGroups, comparingGroups);
        Float result = (float)commonGroups / ((float)(actorGroups.size() + comparingGroups.size() - commonGroups));
        return result;
    }
}
