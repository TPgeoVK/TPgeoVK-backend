package ru.tpgeovk.back.contexts;

import com.google.maps.GeoApiContext;

public class GoogleContext {

    private static final String ENV_GOOGLE_KEY = "TPGEOVK_GOOGLE_API_KEY";

    private static final String GOOGLE_API_KEY;

    private static GeoApiContext GEO_API_CONTEXT;

    static {
        GOOGLE_API_KEY = System.getenv(ENV_GOOGLE_KEY);
        if (GOOGLE_API_KEY == null) {
            throw new RuntimeException("Environment variable TPGEOVK_GOOGLE_API_KEY is wrong or not set");
        }

        GEO_API_CONTEXT = new GeoApiContext.Builder()
                .apiKey(GOOGLE_API_KEY)
                .build();
    }

    public static GeoApiContext getGeoApiContext() { return GEO_API_CONTEXT;}
}
