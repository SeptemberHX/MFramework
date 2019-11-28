package com.septemberhx.server.utils;

import org.apache.commons.lang.RandomStringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
public class MIDUtils {

    private static Set<String> usedInstanceRandomIdSet = new HashSet<>();

    public static void reset(List<String> existedInstanceIdList) {
        usedInstanceRandomIdSet.clear();
        for (String instanceId : existedInstanceIdList) {
            usedInstanceRandomIdSet.add(instanceId.substring(instanceId.lastIndexOf('-') + 1));
        }
    }

    /**
     * The instance id is built with three parts: nodeID, serviceId, noId. The noId should start from 1 and increases
     *   by 1 each time. When create a new instance, the smallest usable noId should be used.
     *
     *   The instance id is consist of : HOSTNAME that the container running or + '-' + serviceId + '-' + random alphanumeric suffix
     *
     * @param nodeId: node id
     * @param serviceId: service Id
     * @return String
     */
    public static String generateInstanceId(String nodeId, String serviceId) {
        String randomSuffix = null;
        do {
            randomSuffix = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        } while (usedInstanceRandomIdSet.contains(randomSuffix));
        usedInstanceRandomIdSet.add(randomSuffix);
        return String.format("%s-%s-%s", nodeId, serviceId, randomSuffix);
    }

    // ATTENTION: DO NOT USE THIS FUNCTION because '-' can be in the hostname !!!
    public static String getNodeIdFromInstanceId(String instanceId) {
        if (instanceId == null) {
            return "";
        }
        return instanceId.split("-")[0];
    }

    /*
     * Get the service id from the instance id.
     */
    public static String getServiceIdFromInstanceId(String instanceId, String serviceId) {
        return instanceId.substring(serviceId.length() + 1, instanceId.lastIndexOf('-'));
    }

    public static String generateSpecificInstanceIdForTest(String nodeId, String serviceId) {
        return String.format("%s_%s", nodeId, serviceId);
    }

    // The serviceName should be unique
    public static String generateServiceId(String serviceName, String rId) {
        return serviceName + "-" + rId;
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

    public static String tranClusterInstanceIdToOurs(String clusterInstanceId) {
        return clusterInstanceId.substring(0, clusterInstanceId.lastIndexOf('-'));
    }

    public static String tranSpringCloudIdToOurs(String springId) {
        return springId.substring(0, springId.indexOf(':'));
    }
}
