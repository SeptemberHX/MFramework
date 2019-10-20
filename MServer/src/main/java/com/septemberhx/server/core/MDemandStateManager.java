package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MServiceInterface;
import com.septemberhx.server.base.model.MUserDemand;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MDemandStateManager extends MObjectManager<MDemandState> {

    public MDemandStateManager shallowClone() {
        MDemandStateManager cloneObject = new MDemandStateManager();
        Map<String, MDemandState> cloneObjMap = new HashMap<>(this.objectMap);
        cloneObject.setObjectMap(cloneObjMap);
        return cloneObject;
    }

    public void deleteById(String demandId) {
        this.objectMap.remove(demandId);
    }

    public void add(MDemandState demandState) {
        this.objectMap.put(demandState.getId(), demandState);
    }

    public List<MDemandState> getDemandStateByInstanceId(String instanceId) {
        List<MDemandState> resultList = new ArrayList<>();
        for (MDemandState demandState : this.objectMap.values()) {
            if (demandState.isAssigned() && demandState.getInstanceId().equals(instanceId)) {
                resultList.add(demandState);
            }
        }
        return resultList;
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
        return this.objectMap.values().stream().filter(s -> s.getInstanceId().equals(instanceId)).collect(Collectors.toList());
    }

    public void replace(MDemandState newState) {
        this.objectMap.put(newState.getId(), newState);
    }
}
