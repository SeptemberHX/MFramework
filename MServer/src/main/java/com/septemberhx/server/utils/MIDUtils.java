package com.septemberhx.server.utils;

import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
public class MIDUtils {

    public static String generateInstanceId(String nodeId, String serviceId) {
        return String.format("%s_%s_%s", nodeId, serviceId, UUID.randomUUID());
    }
}
