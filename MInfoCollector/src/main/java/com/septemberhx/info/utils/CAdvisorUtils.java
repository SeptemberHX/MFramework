package com.septemberhx.info.utils;

import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class CAdvisorUtils {

    private static String API_PREFIX = "/api";
    private static String _VERSION = "/v1.3";
    private static String _MACHINE_STATE_URL = "/machine";

    private static String MACHINE_STATE_URL = API_PREFIX + _VERSION + _MACHINE_STATE_URL;

    public static JSONObject getMachineState(String ipAddr, Integer port) {
        URI uri = MUrlUtils.getRemoteUri(ipAddr, port, MACHINE_STATE_URL);
        String result = MRequestUtils.sendRequest(uri, null, String.class, RequestMethod.GET);
        return new JSONObject(result);
    }
}
