package com.septemberhx.server.core;

import com.septemberhx.common.base.MServerNode;
import com.septemberhx.common.base.MService;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MSystemIndex;
import com.septemberhx.server.utils.MIDUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static Logger logger = LogManager.getLogger(MSystemModel.class);

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
            MServerNode node = MSystemModel.getIns().getMSNManager().getByIp(instanceInfo.getDockerInfo().getHostIp());
            if (node != null) {
                nodeId = node.getId();
            }

            // check if the instance is alive. The mObjectIdMap will not be null if alive
            String ourInstanceId = instanceInfo.getDockerInfo().getInstanceId();
            if (instanceInfo.getMObjectIdMap() != null) {
                MServiceInstance instance = new MServiceInstance(
                        instanceInfo.getParentIdMap(),
                        nodeId,
                        instanceInfo.getIp(),
                        instanceInfo.getPort(),
                        ourInstanceId,
                        instanceInfo.getMObjectIdMap(),
                        instanceInfo.getId(),
                        instanceInfo.getId(),
                        instanceInfo.getDockerInfo().getInstanceId()
                );

                // get actual serviceId of the service instance
                if (MSystemModel.getIns().getOperator().getInstanceManager().containsById(ourInstanceId)) {
                    MServiceInstance currInstance = MSystemModel.getIns().getOperator().getInstanceById(ourInstanceId);
                    instance.setServiceId(currInstance.getServiceId());
                    instance.setServiceName(currInstance.getServiceName());
                } else {  // this means the instance was created in before running. We have to get the real serviceId of it
                    String serviceId = MIDUtils.getServiceIdFromInstanceId(ourInstanceId, nodeId);
                    Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(serviceId);
                    if (serviceOptional.isPresent()) {
                        instance.setServiceName(serviceOptional.get().getServiceName());
                        instance.setServiceId(serviceOptional.get().getId());
                    }
                }

                this.mSIManager.add(instance);
                logger.info(String.format("Instance %s is added to the instance map in server", ourInstanceId));
            }
        } else {
            String ourInstanceId = MIDUtils.tranSpringCloudIdToOurs(instanceInfo.getId());
            if (this.mSIManager.containsById(ourInstanceId)){
                // remove the useless info when the instance is dead
                this.mSIManager.delete(ourInstanceId);
                logger.info(String.format("Instance %s is deleted due to empty docker info", ourInstanceId));
            }
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
