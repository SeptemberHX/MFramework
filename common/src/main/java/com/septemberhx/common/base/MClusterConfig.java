package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

public class MClusterConfig {

    @Getter
    @Setter
    private String mClusterHost;

    @Getter
    @Setter
    private int mClusterPort;

    @Getter
    @Setter
    private String mBuilderUrl;

    public final static String MCLUSTER_FETCH_INSTANCE_INFO = "/magent/instanceInfoList";
    public final static String MCLUSTERAGENT_REQUEST_REMOTE_URI = "/magent/remoteuri";
    public final static String MCLUSTER_SET_REST_INFO = "/mclient/setRestInfo";
    public final static String MCLUSTERAGENT_SET_REST_INFO = "/magent/setRestInfo";

    public final static String MCLUSTERAGENT_FETCH_CLIENT_INFO = "/mclient/info";
    public final static String MSERVER_GET_REMOTE_URI = "/mserver/remoteuri";
    public final static String MCLUSTER_DOCKER_NAMESPACE = "kube-test";

    public final static String MCLUSTER_SERVICE_METADATA_NAME = "mclient";
    public final static String MCLUSTER_SERVICE_METADATA_VALUE = "true";

    private static MClusterConfig ourInstance = new MClusterConfig();

    public static MClusterConfig getInstance() {
        return ourInstance;
    }

    private MClusterConfig() {
        this.setMClusterHost("192.168.1.102");
        this.setMClusterPort(9000);
    }
}
