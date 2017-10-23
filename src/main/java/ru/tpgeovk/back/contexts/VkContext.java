package ru.tpgeovk.back.contexts;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

public class VkContext {

    private static final String ENV_APP_ID =  "TPGEOVK_VK_APP_ID";
    private static final String ENV_SECURE_KEY = "TPGEOVK_VK_SECURE_KEY";
    private static final String ENV_ACCESS_KEY = "TPGEOVK_VK_ACCESS_KEY";

    private static final Integer APP_ID;
    private static final String SECURE_KEY;
    private static final String ACCESS_KEY;

    private static final String SCOPE = "friends,pages,notes,wall,groups,offline";
    private static final String API_VERSION = "5.68";

    private static final VkApiClient VK_API_CLIENT;

    static {
        if (System.getenv("TPGEOVK_VK_APP_ID") == null) {
            throw new RuntimeException("Environment variable TPGEOVK_VK_APP_ID is wrong or not set");
        }
        APP_ID = Integer.valueOf(System.getenv(ENV_APP_ID));

        SECURE_KEY = System.getenv(ENV_SECURE_KEY);
        if (SECURE_KEY == null) {
            throw new RuntimeException("Environment variable TPGEOVK_VK_SECURE_KEY is wrong or not set");
        }

        ACCESS_KEY = System.getenv(ENV_ACCESS_KEY);
        if (ACCESS_KEY == null) {
            throw new RuntimeException("Environment variable TPGEOVK_VK_ACCESS_KEY is wrong or not set");
        }

        VK_API_CLIENT = new VkApiClient(new HttpTransportClient());
    }

    public static Integer getAppId() {
        return APP_ID;
    }

    public static String getSecureKey() {
        return SECURE_KEY;
    }

    public static String getAccessKey() {
        return ACCESS_KEY;
    }

    public static String getScope() { return SCOPE; }

    public static String getApiVersion() { return API_VERSION; }

    public static VkApiClient getVkApiClient() {
        return VK_API_CLIENT;
    }
}
