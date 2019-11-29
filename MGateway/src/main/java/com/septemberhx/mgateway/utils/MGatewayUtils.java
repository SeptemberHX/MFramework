package com.septemberhx.mgateway.utils;

import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.mgateway.client.MClusterAgentClient;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/29
 */
public class MGatewayUtils {

    public static MClusterAgentClient clusterAgentClient;

    public static String fetchRequestUrl(MUserDemand userDemand) {
        return clusterAgentClient.fetchRequestUrl(userDemand);
    }
}
