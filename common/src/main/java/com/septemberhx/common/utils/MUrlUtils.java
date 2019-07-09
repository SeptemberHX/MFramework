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
            return MUrlUtils.getRemoteUri(
                    MClusterConfig.getInstance().getMClusterHost(),
                    MClusterConfig.getInstance().getMClusterPort(),
                    MClusterConfig.MCLUSTER_FETCH_INSTANCE_INFO
            );
    }

    /**
     * Construct the uri to fetch the parentIdMap for given INSTANCE IP ADDRESS
     * @param instanceIpAddr: ip address of given instance
     * @param port: service port
     * @return URI
     */
    public static URI getMClusterAgentFetchClientInfoUri(String instanceIpAddr, int port) {
        return MUrlUtils.getRemoteUri(instanceIpAddr, port, MClusterConfig.MCLUSTERAGENT_FETCH_CLIENT_INFO);
    }

    public static URI getMClientRequestRemoteUri(String clusterAgentIpAddr, int port) {
        return MUrlUtils.getRemoteUri(clusterAgentIpAddr, port, MClusterConfig.MCLUSTERAGENT_REQUEST_REMOTE_URI);
    }

    public static URI getMClientAgentSetRestInfoUri(String clusterAgentIpAddr, int port) {
        return MUrlUtils.getRemoteUri(clusterAgentIpAddr, port, MClusterConfig.MCLUSTERAGENT_SET_REST_INFO);
    }

    public static URI getMServerRemoteUri(String serverIpAddr, int serverPort) {
        return MUrlUtils.getRemoteUri(serverIpAddr, serverPort, MClusterConfig.MSERVER_GET_REMOTE_URI);
    }

    public static URI getMServerLoadInstanceInfoUri(String serverIpAddr, int serverPort) {
        return MUrlUtils.getRemoteUri(serverIpAddr, serverPort, MClusterConfig.MSERVER_LOAD_INSTANCEINFO);
    }

    public static URI getRemoteUri(String ipAddr, int port, String path) {
        URI uri = null;
        try {
            uri = new URI(
                    "http",
                    null,
                    ipAddr,
                    port,
                    path, null, null
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        logger.debug(uri);
        return uri;
    }

    public static URI getMClusterSetRestInfoUri(String instanceIpAddr, int port) {
        return MUrlUtils.getRemoteUri(instanceIpAddr, port, MClusterConfig.MCLUSTER_SET_REST_INFO);
    }
}
