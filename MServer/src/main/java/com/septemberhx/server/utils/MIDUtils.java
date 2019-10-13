package com.septemberhx.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
     * @param idList: current id lists of given service on target node
     * @return String
     */
    public static String generateInstanceId(String nodeId, String serviceId, List<String> idList) {
        List<Integer> idNoList = new ArrayList<>();
        for (String instanceId : idList) {
            String[] splitR = instanceId.split("_");
            idNoList.add(Integer.valueOf(splitR[splitR.length - 1]));
        }
        Collections.sort(idNoList);
        int targetNo = 0;
        for (; targetNo != idNoList.size(); ++targetNo) {
            if (targetNo != idNoList.get(targetNo)) {
                break;
            }
        }

        return String.format("%s_%s_%04d", nodeId, serviceId, targetNo);
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
