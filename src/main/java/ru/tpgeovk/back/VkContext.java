package ru.tpgeovk.back;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

public class VkContext {

    /** Keep it in secret! */
    private static final Integer APP_ID = 6179799;
    private static final String SECURE_KEY = "w8vcGPsBRLAZmOr2bTEM";
    private static final String ACCESS_KEY = "a0380773a0380773a03807734ba0664ca4aa038a0380773f98d15bfa17f5eb6731ffd07";

    private static final VkApiClient VK_API_CLIENT = new VkApiClient(new HttpTransportClient());

    public static Integer getAppId() {
        return APP_ID;
    }

    public static String getSecureKey() {
        return SECURE_KEY;
    }

    public static String getAccessKey() {
        return ACCESS_KEY;
    }

    public static VkApiClient getVkApiClient() {
        return VK_API_CLIENT;
    }
}
