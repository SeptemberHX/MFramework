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
    public final static String MCLUSTERAGENT_FETCH_CLIENT_INFO = "/mclient/info";
    public final static String MCLUSTER_DOCKER_NAMESPACE = "kube-test";

    private static MClusterConfig ourInstance = new MClusterConfig();

    public static MClusterConfig getInstance() {
        return ourInstance;
    }

    private MClusterConfig() {
        this.setMClusterHost("192.168.1.102");
        this.setMClusterPort(46832);
    }
}
