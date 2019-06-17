package com.septemberhx.agent.utils;

import com.septemberhx.common.bean.MInstanceParentIdMapResponse;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class MClientUtils {

    private static final String MCLIENTPORT = "8080";
    private static RestTemplate restTemplate = new RestTemplate();

    public static Map<String, String> getParentIdMap(String serverIp) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            MInstanceParentIdMapResponse parentIdMapResponse
                    = restTemplate.getForObject(serverIp + ":" + MCLIENTPORT, MInstanceParentIdMapResponse.class);
            resultMap = parentIdMapResponse.getParentIdMap();
        } catch (Exception e) {
            e.printStackTrace();
            return resultMap;
        }
        return resultMap;
    }
}
