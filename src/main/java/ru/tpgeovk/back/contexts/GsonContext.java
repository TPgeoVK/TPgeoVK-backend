package ru.tpgeovk.back.contexts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.tpgeovk.back.model.UserFeatures;
import ru.tpgeovk.back.model.deserializer.UserFeaturesDeserializer;

public class GsonContext {

    public static final Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(UserFeatures.class, new UserFeaturesDeserializer())
                .create();
    }
}
