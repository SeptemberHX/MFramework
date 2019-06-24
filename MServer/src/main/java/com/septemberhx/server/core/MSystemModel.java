package com.septemberhx.server.core;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.MServiceInstance;

import java.util.Optional;


public class MSystemModel {

    private MServiceInstanceManager mSIManager;
    private MServerNodeManager mSNManager;

    public MSystemModel() {
        this.mSIManager = new MServiceInstanceManager();
        this.mSNManager = new MServerNodeManager();
    }

    public void loadInstanceInfo(MInstanceInfoBean instanceInfo, String instanceIp) {
        this.mSIManager.add(new MServiceInstance(
                instanceInfo.getParentIdMap(),
                instanceInfo.getDockerInfo().getHostIp(),
                instanceInfo.getIp(),
                instanceInfo.getPort(),
                instanceIp
        ));
    }

    public Optional<MServiceInstance> getInstanceById(String instanceId) {
        return this.mSIManager.getById(instanceId);
    }
}
