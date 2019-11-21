package com.septemberhx.server.core;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MSystemIndex;
import com.septemberhx.server.utils.MIDUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * This class stores everything the system needs.
 * Basically speaking, the snapshot of this class is the snapshot of the system. (Except for inner states now)
 */
public class MSystemModel {

    @Getter
    @Setter
    private MServiceInstanceManager mSIManager;

    @Getter
    @Setter
    private MServerNodeManager mSNManager;

    @Getter
    @Setter
    private MUserManager userManager;

    @Getter
    @Setter
    private MRepoManager repoManager;

    @Getter
    @Setter
    private MDemandStateManager demandStateManager;

    @Getter
    @Setter
    private MServiceManager serviceManager;

    @Getter
    @Setter
    private MSystemIndex lastSystemIndex;

    @Getter
    @Setter
    private MServerOperator operator;

    private static MSystemModel ourInstance = new MSystemModel();

    public static MSystemModel getIns() {
        return ourInstance;
    }

    private MSystemModel() {
        this.mSIManager = new MServiceInstanceManager();
        this.mSNManager = new MServerNodeManager();
        this.userManager = new MUserManager();
        this.repoManager = new MRepoManager();
        this.demandStateManager = new MDemandStateManager();
        this.serviceManager = new MServiceManager();
        this.lastSystemIndex = new MSystemIndex();
        this.operator = new MServerOperator();
    }

    public void loadInstanceInfo(MInstanceInfoBean instanceInfo) {
        String nodeId = null;
        if (instanceInfo.getDockerInfo() != null) {
            nodeId = instanceInfo.getDockerInfo().getHostIp();
        }

        // check if the instance is alive. The mObjectIdMap will not be null if alive
        // todo: get actual serviceId of the service instance
        String ourInstanceId = MIDUtils.tranClusterInstanceIdToOurs(instanceInfo.getDockerInfo().getInstanceId());
        if (instanceInfo.getMObjectIdMap() != null) {
            this.mSIManager.update(new MServiceInstance(
                    instanceInfo.getParentIdMap(),
                    nodeId,
                    instanceInfo.getIp(),
                    instanceInfo.getPort(),
                    ourInstanceId,
                    instanceInfo.getMObjectIdMap(),
                    instanceInfo.getId(),
                    instanceInfo.getId(),
                    instanceInfo.getDockerInfo().getInstanceId()
            ));
        } else if (this.mSIManager.containsById(ourInstanceId)){
            // remove the useless info when the instance is dead
            this.mSIManager.remove(ourInstanceId);
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
