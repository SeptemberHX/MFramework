package com.septemberhx.agent.utils;

import com.septemberhx.common.bean.MInstanceApiMapResponse;
import com.septemberhx.common.bean.MInstanceParentIdMapResponse;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MClientUtils {

    private static final String MCLIENTPORT = "8081";
    private static RestTemplate restTemplate = new RestTemplate();

    public static Map<String, String> getParentIdMap(String serverIp) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            MInstanceParentIdMapResponse parentIdMapResponse
                    = restTemplate.getForObject("http://" + serverIp + ":" + MCLIENTPORT +  "/mclient/getParentIdMap",
                                                MInstanceParentIdMapResponse.class);
            resultMap = parentIdMapResponse.getParentIdMap();
        } catch (Exception e) {
            e.printStackTrace();
            return resultMap;
        }
        return resultMap;
    }

    public static Map<String, Set<String>> getApiMap(String serverIp) {
        Map<String, Set<String>> resultMap = new HashMap<>();
        try {
            MInstanceApiMapResponse response
                    = restTemplate.getForObject("http://" + serverIp + ":" + MCLIENTPORT +  "/mclient/getApiMap",
                    MInstanceApiMapResponse.class);
            resultMap = response.getApiMap();
        } catch (Exception e) {
            e.printStackTrace();
            return resultMap;
        }
        return resultMap;
    }
}
