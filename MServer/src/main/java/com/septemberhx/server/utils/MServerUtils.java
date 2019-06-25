package com.septemberhx.server.utils;

import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.RequestUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

public class MServerUtils {

    private static RestTemplate restTemplate = new RestTemplate();

    public static MInstanceInfoResponse fetchAllInstanceInfo() {
        return RequestUtils.sendRequest(
                MUrlUtils.getMclusterFetchInstanceInfoUri(),
                null,
                MInstanceInfoResponse.class,
                RequestMethod.GET);
    }

    public static void notifyAddNewRemoteUri(String ipAddr, String mObjectId, String funcName) {

    }

    public static void notifyDeleteRemoteUri(String ipAddr, String mObjectId, String funcName) {

    }
}
