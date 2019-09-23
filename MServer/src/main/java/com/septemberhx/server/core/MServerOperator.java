package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
public class MServerOperator extends MObjectManager<MServerState> {

    private Map<String, Integer> instanceId2UserCap;    // left user capability of each instance current time
    private Map<String, Integer> preInstanceId2UserCap; // left user capability after operations.
                                                        // For planning only because no action will be done when planning
                                                        // All actions should be done when executing.

    private Map<String, MResource> nodeId2ResourceLeft;
    private Map<String, MResource> preNodeId2ResourceLeft;

    public MServerOperator() {
        this.instanceId2UserCap = new HashMap<>();
        this.preInstanceId2UserCap = new HashMap<>();
        this.nodeId2ResourceLeft = new HashMap<>();
        this.preNodeId2ResourceLeft = new HashMap<>();
    }

    /**
     * This function will init all member variables which are used by other functions.
     * SHOULD BE CALLED BEFORE ANY OTHER OPERATIONS.
     */
    public void reInit() {
        this.instanceId2UserCap.clear();
        this.preInstanceId2UserCap.clear();

        Map<String, Integer> insId2UserNum = new HashMap<>();
        for (MDemandState demandState : MSystemModel.getInstance().getDemandStateManager().getAllValues()) {
            if (!insId2UserNum.containsKey(demandState.getInstanceId())) {
                insId2UserNum.put(demandState.getInstanceId(), 0);
            }
            insId2UserNum.put(demandState.getInstanceId(), 1 + insId2UserNum.get(demandState.getInstanceId()));
        }

        for (String instanceId : insId2UserNum.keySet()) {
            Optional<MServiceInstance> instanceOptional = MSystemModel.getInstance().getInstanceById(instanceId);
            if (instanceOptional.isPresent()) {
                Optional<MService> serviceOptional = MSystemModel.getInstance().getServiceManager().getById(instanceOptional.get().getServiceId());
                serviceOptional.ifPresent(mService ->
                    instanceId2UserCap.put(instanceId, mService.getMaxUserCap() - instanceId2UserCap.get(instanceId))
                );
            }
        }

        this.nodeId2ResourceLeft.clear();
        this.preNodeId2ResourceLeft.clear();
        for (MServerState serverState : this.objectMap.values()) {
            Optional<MServerNode> nodeOptional = MSystemModel.getInstance().getMSNManager().getById(serverState.getId());
            nodeOptional.ifPresent(serverNode ->
                this.nodeId2ResourceLeft.put(serverState.getId(), serverNode.getResource().sub(serverState.getResource()))
            );
        }
    }

    public boolean ifNodeHasResForIns(String nodeId, String serviceId) {
        Optional<MService> serviceOptional = MSystemModel.getInstance().getServiceManager().getById(serviceId);
        if (!serviceOptional.isPresent()) {
            return false;
        }
        MService service = serviceOptional.get();

        return this.getNodeResourceLeft(nodeId).isEnough(service.getResource());
    }

    private MResource getNodeResourceLeft(String nodeId) {
        MResource resourceLeft;
        if (this.preNodeId2ResourceLeft.containsKey(nodeId)) {
            resourceLeft = this.preNodeId2ResourceLeft.get(nodeId);
        } else {
            resourceLeft = this.nodeId2ResourceLeft.get(nodeId);
        }
        return resourceLeft;
    }

    private Integer getUserCapLeft(String instanceId) {
        Integer leftUserCap;
        if (this.preInstanceId2UserCap.containsKey(instanceId)) {
            leftUserCap = this.preInstanceId2UserCap.get(instanceId);
        } else {
            leftUserCap = this.instanceId2UserCap.get(instanceId);
        }
        return leftUserCap;
    }
}
