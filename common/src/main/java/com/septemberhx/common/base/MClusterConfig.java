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
    public final static String MCLIENT_SET_APICS_URI = "/mclient/setApiContinueStatus";
    public final static String MCLUSTERAGENT_SET_REST_INFO = "/magent/setRestInfo";
    public final static String MCLUSTERAGENT_DEPLOY_URI = "/magent/deploy";
    public final static String MCLUSTERAGENT_SET_APICS_URI = "/magent/setApiContinueStatus";
    public final static String MCLUSTERAGENT_FETCH_LOGS = "/magent/fetchLogsBetweenTime";

    public final static String MCLUSTERAGENT_FETCH_CLIENT_INFO = "/mclient/info";
    public final static String MSERVER_GET_REMOTE_URI = "/mserver/remoteuri";
    public final static String MSERVER_LOAD_INSTANCEINFO = "/mserver/loadInstanceInfo";
    public final static String MSERVER_JOB_NOTIFY_URI = "/mserver/notifyJob";
    public final static String MSERVER_DEPLOY_JOB_NOTIFY_URI = "/mserver/notifyDeployJob";

    public final static String MCLUSTER_DOCKER_NAMESPACE = "kube-test";

    public final static String MCLUSTER_SERVICE_METADATA_NAME = "mclient";
    public final static String MCLUSTER_SERVICE_METADATA_VALUE = "true";

    public final static String BUILD_CENTER_BUILD_URI = "/buildcenter/build";
    public final static String BUILD_CENTER_CBUILD_URI = "/buildcenter/cbuild";


    public final static String K8S_NAMESPACE = "kube-test";

    private static MClusterConfig ourInstance = new MClusterConfig();

    public static MClusterConfig getInstance() {
        return ourInstance;
    }

    private MClusterConfig() {
        this.setMClusterHost("192.168.1.102");
        this.setMClusterPort(9000);
    }
}
