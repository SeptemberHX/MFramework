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

    public static String generateSpecificInstanceIdForTest(String nodeId, String serviceId) {
        return String.format("%s_%s", nodeId, serviceId);
    }

    // The serviceName should be unique
    public static String generateServiceId(String serviceName) {
        return serviceName;
    }

    public static String generateInterfaceId(String serviceId, String interfaceName) {
        return String.format("%s_%s", serviceId, interfaceName);
    }

    public static String generateFunctionId(String functionName) {
        return String.format("%s_%s", functionName, UUID.randomUUID());
    }
}
