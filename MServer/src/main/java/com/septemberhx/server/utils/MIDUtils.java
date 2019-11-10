package com.septemberhx.server.utils;

import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
public class MIDUtils {

    /**
     * The instance id is built with three parts: nodeID, serviceId, noId. The noId should start from 1 and increases
     *   by 1 each time. When create a new instance, the smallest usable noId should be used.
     * @param nodeId: node id
     * @param serviceId: service Id
     * @return String
     */
    public static String generateInstanceId(String nodeId, String serviceId) {
        return String.format("%s_%s_%s", nodeId, serviceId, UUID.randomUUID());
    }

    public static String getNodeIdFromInstanceId(String instanceId) {
        if (instanceId == null) {
            return "";
        }
        return instanceId.split("_")[0];
    }

    public static String generateSpecificInstanceIdForTest(String nodeId, String serviceId) {
        return String.format("%s_%s", nodeId, serviceId);
    }

    // The serviceName should be unique
    public static String generateServiceId(String serviceName, String rId) {
        return serviceName + "_" + rId;
    }

    public static String generateInterfaceId(String serviceId, String interfaceName) {
        return String.format("%s_%s", serviceId, interfaceName);
    }

    public static String generateFunctionId(String functionName) {
        return String.format("%s_%s", functionName, UUID.randomUUID());
    }

    public static String generateComDemandAssignId() {
        return UUID.randomUUID().toString();
    }
}
