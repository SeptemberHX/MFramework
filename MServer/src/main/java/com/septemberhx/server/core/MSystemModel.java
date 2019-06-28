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

    public void loadInstanceInfo(MInstanceInfoBean instanceInfo) {
        String nodeId = null;
        if (instanceInfo.getDockerInfo() != null) {
            nodeId = instanceInfo.getDockerInfo().getHostIp();
        }

        this.mSIManager.add(new MServiceInstance(
                instanceInfo.getParentIdMap(),
                nodeId,
                instanceInfo.getIp(),
                instanceInfo.getPort(),
                instanceInfo.getId(),
                instanceInfo.getMObjectIdMap()
        ));
    }

    public Optional<MServiceInstance> getInstanceById(String instanceId) {
        return this.mSIManager.getById(instanceId);
    }

    public Optional<MServiceInstance> getInstanceByMObjectId(String mObjectId) {
        return this.mSIManager.getInstanceByMObjectId(mObjectId);
    }
}
