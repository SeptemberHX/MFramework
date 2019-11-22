package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.common.base.MServiceInterface;
import com.septemberhx.common.base.MUserDemand;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MDemandStateManager extends MObjectManager<MDemandState> {

    @Getter
    private Map<String, List<String>> instanceId2StateIdList;

    public MDemandStateManager() {
        this.instanceId2StateIdList = new HashMap<>();
    }

    public MDemandStateManager shallowClone() {
        MDemandStateManager cloneObject = new MDemandStateManager();
        Map<String, MDemandState> cloneObjMap = new HashMap<>(this.objectMap);
        Map<String, List<String>> cloneObjMap2List = new HashMap<>();
        for (String instanceId : this.instanceId2StateIdList.keySet()) {
            cloneObjMap2List.put(instanceId, new ArrayList<>(this.instanceId2StateIdList.get(instanceId)));
        }
        cloneObject.setObjectMap(cloneObjMap);
        cloneObject.instanceId2StateIdList = cloneObjMap2List;
        return cloneObject;
    }

    public void deleteById(String demandId) {
        if (this.objectMap.containsKey(demandId)) {
            MDemandState demandState = this.objectMap.get(demandId);
            this.instanceId2StateIdList.get(demandState.getInstanceId()).remove(demandState.getId());
            if (this.instanceId2StateIdList.get(demandState.getInstanceId()).isEmpty()) {
                this.instanceId2StateIdList.remove(demandState.getInstanceId());
            }
        }
        this.objectMap.remove(demandId);
    }

    public void add(MDemandState demandState) {
        this.objectMap.put(demandState.getId(), demandState);
        if (!this.instanceId2StateIdList.containsKey(demandState.getInstanceId())) {
            this.instanceId2StateIdList.put(demandState.getInstanceId(), new ArrayList<>());
        }
        this.instanceId2StateIdList.get(demandState.getInstanceId()).add(demandState.getId());
    }

    public List<MDemandState> getDemandStateByInterfaceId(String interfaceId) {
        List<MDemandState> resultList = new ArrayList<>();
        for (MDemandState demandState : this.objectMap.values()) {
            if (demandState.isAssigned() && demandState.getInterfaceId().equals(interfaceId)) {
                resultList.add(demandState);
            }
        }
        return resultList;
    }

    public static boolean checkIfDemandSatisfied(MDemandState demandState) {
        MUserDemand userDemand = MSystemModel.getIns().getUserManager()
                                        .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getId());
        Optional<MServiceInstance> instanceOptional = MSystemModel.getIns().getInstanceById(demandState.getInstanceId());
        if (instanceOptional.isPresent()) {
            MServiceInterface serviceInterface = MSystemModel.getIns().getServiceManager()
                    .getServiceInterfaceByServiceAndInterfaceId(instanceOptional.get().getServiceId(), demandState.getInterfaceId());
            return userDemand.isServiceInterfaceMet(serviceInterface);
        }
        return false;
    }

    public List<MDemandState> getDemandStatesOnNode(String nodeId) {
        return this.objectMap.values().stream().filter(s -> s.getNodeId().equals(nodeId)).collect(Collectors.toList());
    }

    public List<MDemandState> getDemandStatesOnInstance(String instanceId) {
        List<MDemandState> result = new ArrayList<>();
        for (String demandStateId : this.instanceId2StateIdList.getOrDefault(instanceId, new ArrayList<>())) {
            if (this.objectMap.containsKey(demandStateId)) {
                result.add(this.objectMap.get(demandStateId));
            }
        }

//        List<MDemandState> result1 = this.objectMap.values().stream().filter(d -> d.getInstanceId().equals(instanceId)).collect(Collectors.toList());
//        if (result1.size() != result.size()) {
//            logger.error("MDemandState size not the same.");
//        }
//        return result;
        return result;
    }

    public void replace(MDemandState newState) {
        MDemandState oldState = this.objectMap.get(newState.getId());
        if (!oldState.getInstanceId().equals(newState.getInstanceId())) {
            this.instanceId2StateIdList.get(oldState.getInstanceId()).remove(oldState.getId());
            if (!this.instanceId2StateIdList.containsKey(newState.getInstanceId())) {
                this.instanceId2StateIdList.put(newState.getInstanceId(), new ArrayList<>());
            }
            this.instanceId2StateIdList.get(newState.getInstanceId()).add(newState.getId());
        }
        this.objectMap.put(newState.getId(), newState);
    }
}
