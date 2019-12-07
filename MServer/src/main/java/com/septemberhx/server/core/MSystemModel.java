package com.septemberhx.server.core;

import com.septemberhx.common.base.MResource;
import com.septemberhx.common.base.MServerNode;
import com.septemberhx.common.base.MService;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.base.MSystemInfoBean;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MSystemIndex;
import com.septemberhx.server.utils.MIDUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
                    } else {
                        return;  // if we can't recognise the service, then ignore it
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

    public MSystemInfoBean getSystemInfo() {
        MSystemInfoBean infoBean = new MSystemInfoBean();
        List<MUserDemand> userDemandList = this.userManager.getAllUserDemands();
        infoBean.setTotalDemandNum(userDemandList.size());

        Set<String> serviceNameSet = userDemandList.stream().map(MUserDemand::getServiceId).collect(Collectors.toSet());
        infoBean.setTotalDemandServiceNum(serviceNameSet.size());

        Map<String, Map<String, Set<Integer>>> serviceName2Function2SlaMap = new HashMap<>();
        for (MUserDemand userDemand : userDemandList) {
            if (!serviceName2Function2SlaMap.containsKey(userDemand.getServiceId())) {
                serviceName2Function2SlaMap.put(userDemand.getServiceId(), new HashMap<>());
            }

            if (!serviceName2Function2SlaMap.get(userDemand.getServiceId()).containsKey(userDemand.getFunctionId())) {
                serviceName2Function2SlaMap.get(userDemand.getServiceId()).put(userDemand.getFunctionId(), new HashSet<>());
            }
            serviceName2Function2SlaMap.get(userDemand.getServiceId()).get(userDemand.getFunctionId()).add(userDemand.getSlaLevel());
        }
        int totalDemandKindSize = 0;
        for (String serviceName : serviceName2Function2SlaMap.keySet()) {
            for (String functionId : serviceName2Function2SlaMap.get(serviceName).keySet()) {
                totalDemandKindSize += serviceName2Function2SlaMap.get(serviceName).get(functionId).size();
            }
        }
        infoBean.setTotalDemandKindNum(totalDemandKindSize);

        for (MServerNode node : this.mSNManager.getAllValues()) {
            MResource usedResource = new MResource();
            List<MServiceInstance> instanceList = this.mSIManager.getInstancesOnNode(node.getId());
            for (MServiceInstance instance : instanceList) {
                MService service = this.serviceManager.getById(instance.getServiceId()).get();
                usedResource = usedResource.add(service.getResource());
            }
            infoBean.getNodeCpuUsagePercentMap().put(node.getId(), usedResource.getCpu() * 1.0 / node.getResource().getCpu());
            infoBean.getNodeRamUsagePercentMap().put(node.getId(), usedResource.getRam() * 1.0 / node.getResource().getRam());
        }
        return infoBean;
    }
}
