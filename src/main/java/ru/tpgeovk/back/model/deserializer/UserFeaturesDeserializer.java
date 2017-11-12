package ru.tpgeovk.back.model.deserializer;

import com.google.gson.*;
import ru.tpgeovk.back.model.UserFeatures;

import java.lang.reflect.Type;
import java.util.List;

public class UserFeaturesDeserializer implements JsonDeserializer<UserFeatures> {


    @Override
    public UserFeatures deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        UserFeatures result = new UserFeatures();

        Integer userId = jsonObject.getAsJsonPrimitive("userId").getAsInt();
        result.setUserId(userId);

        Boolean gender = jsonObject.getAsJsonPrimitive("gender").getAsInt() == 1 ? Boolean.TRUE :
                Boolean.FALSE;
        result.setGender(gender);

        JsonElement groupsField = jsonObject.get("groups");
        if (groupsField.isJsonArray()) {
            JsonArray groupsArray = groupsField.getAsJsonArray();
            List<String> groups = result.getGroups();
            for (JsonElement group : groupsArray) {
                groups.add(group.getAsString());
            }
        }

        JsonElement bdateField = jsonObject.get("bdate");
        if (bdateField.isJsonPrimitive()) {
            String bdate = bdateField.getAsString();
            if (bdate != null) {
                if (bdate.length() > 5) {
                    String yearStr = bdate.substring(bdate.length() - 4);
                    if (!yearStr.contains(".")) {
                        Integer year = Integer.valueOf(yearStr);
                        result.setAge(2017 - year); //TODO: считать точный возраст
                    }
                }
            }
        }

        return result;
    }
}
