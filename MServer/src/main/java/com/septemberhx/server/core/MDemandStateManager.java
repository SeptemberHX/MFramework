package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MServiceInterface;
import com.septemberhx.server.base.model.MUserDemand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MDemandStateManager extends MObjectManager<MDemandState> {

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
        MUserDemand userDemand = MSystemModel.getInstance().getUserManager()
                                        .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getId());
        Optional<MServiceInstance> instanceOptional = MSystemModel.getInstance().getInstanceById(demandState.getInstanceId());
        if (instanceOptional.isPresent()) {
            MServiceInterface serviceInterface = MSystemModel.getInstance().getServiceManager()
                    .getServiceInterfaceByServiceAndInterfaceId(instanceOptional.get().getServiceId(), demandState.getInterfaceId());
            return userDemand.isServiceInterfaceMet(serviceInterface);
        }
        return false;
    }
}
