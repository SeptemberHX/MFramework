package com.septemberhx.common.utils;

import com.septemberhx.common.base.MClusterConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class MUrlUtils {

    private final static Logger logger = LogManager.getLogger(MUrlUtils.class);

    /**
     * Construct the uri to fetch all the instance info in the cluster
     * @return URI
     */
    public static URI getMclusterFetchInstanceInfoUri() {
        URI uri = null;
        try {
            uri = new URI(
                    "http",
                    null,
                    MClusterConfig.getInstance().getMClusterHost(),
                    MClusterConfig.getInstance().getMClusterPort(),
                    MClusterConfig.MCLUSTER_FETCH_INSTANCE_INFO, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        logger.debug(uri);
        return uri;
    }

    /**
     * Construct the uri to fetch the parentIdMap for given INSTANCE IP ADDRESS
     * @param instanceIpAddr: ip address of given instance
     * @param port: service port
     * @return URI
     */
    public static URI getMClusterAgentFetchClientInfoUri(String instanceIpAddr, int port) {
        URI uri = null;
        try {
            uri = new URI(
                    "http",
                    null,
                    instanceIpAddr,
                    port,
                    MClusterConfig.MCLUSTERAGENT_FETCH_CLIENT_INFO, null, null
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        logger.debug(uri);
        return uri;
    }
}
