package com.septemberhx.server.core;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.MServiceInstance;

import java.util.List;
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

        // check if the instance is alive. The mobjectIdMap will not be null if alive
        if (instanceInfo.getMObjectIdMap() != null) {
            this.mSIManager.update(new MServiceInstance(
                    instanceInfo.getParentIdMap(),
                    nodeId,
                    instanceInfo.getIp(),
                    instanceInfo.getPort(),
                    instanceInfo.getId(),
                    instanceInfo.getMObjectIdMap()
            ));
        } else if (this.mSIManager.containsById(instanceInfo.getId())){
            // remove the useless info when the instance is dead
            this.mSIManager.remove(instanceInfo.getId());
        }
    }

    public List<MServiceInstance> getAllServiceInstance() {
        return this.mSIManager.getAllValues();
    }

    public Optional<MServiceInstance> getInstanceById(String instanceId) {
        return this.mSIManager.getById(instanceId);
    }

    public Optional<MServiceInstance> getInstanceByMObjectId(String mObjectId) {
        return this.mSIManager.getInstanceByMObjectId(mObjectId);
    }
}
