package com.septemberhx.server.core;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.model.MServiceInstance;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * This class stores everything the system needs.
 * Basically speaking, the snapshot of this class is the snapshot of the system. (Except for inner states now)
 */
public class MSystemModel {

    @Getter
    private MServiceInstanceManager mSIManager;

    @Getter
    private MServerNodeManager mSNManager;

    @Getter
    private MUserManager userManager;

    @Getter
    private MRepoManager repoManager;

    @Getter
    private MDemandStateManager demandStateManager;

    @Getter
    private MServiceManager serviceManager;

    private static MSystemModel ourInstance = new MSystemModel();

    public static MSystemModel getInstance() {
        return ourInstance;
    }

    private MSystemModel() {
        this.mSIManager = new MServiceInstanceManager();
        this.mSNManager = new MServerNodeManager();
        this.userManager = new MUserManager();
        this.repoManager = new MRepoManager();
        this.demandStateManager = new MDemandStateManager();
        this.serviceManager = new MServiceManager();
    }

    public void loadInstanceInfo(MInstanceInfoBean instanceInfo) {
        String nodeId = null;
        if (instanceInfo.getDockerInfo() != null) {
            nodeId = instanceInfo.getDockerInfo().getHostIp();
        }

        // check if the instance is alive. The mObjectIdMap will not be null if alive
        if (instanceInfo.getMObjectIdMap() != null) {
            this.mSIManager.update(new MServiceInstance(
                    instanceInfo.getParentIdMap(),
                    nodeId,
                    instanceInfo.getIp(),
                    instanceInfo.getPort(),
                    instanceInfo.getId(),
                    instanceInfo.getMObjectIdMap(),
                    instanceInfo.getId()
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
