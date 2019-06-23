package com.septemberhx.server.core;


import com.septemberhx.agent.middleware.MServiceManager;
import com.septemberhx.agent.middleware.MServiceManagerK8SImpl;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.MServiceInstance;

import java.util.Optional;

public class MSystemModel {

    private MServiceInstanceManager mSIManager;
    private MServerNodeManager mSNManager;
    private MServiceManager clusterMiddleware;

    public MSystemModel() {
        this.mSIManager = new MServiceInstanceManager();
        this.mSNManager = new MServerNodeManager();
        this.clusterMiddleware = new MServiceManagerK8SImpl();
    }

    public void loadInstanceInfo(MInstanceInfoBean instanceInfo, String instanceIp) {
        // should query k8s to get the node id
        Optional<String> instanceId = this.clusterMiddleware.getInstanceIdByIp(instanceIp);
        if (instanceId.isPresent()) {
            Optional<String> nodeId = instanceId.flatMap(i -> this.clusterMiddleware.getInstanceIdByIp(i));
            this.mSIManager.add(new MServiceInstance(
                    instanceInfo.getParentIdMap(),
                    nodeId.get(),
                    instanceIp,
                    instanceId.get()
            ));
        } else {
            throw new RuntimeException("Can't fetch the information about InstanceInfo from " + instanceIp + ": "
                    + instanceInfo.toString());
        }
    }
}
