package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MDemandState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MDemandStateManager extends MObjectManager<MDemandState> {

    public List<MDemandState> getDemandStateByInstanceId(String instanceId) {
        List<MDemandState> resultList = new ArrayList<>();
        for (MDemandState demandState : this.objectMap.values()) {
            if (demandState.isSatisfied() && demandState.getInstanceId().equals(instanceId)) {
                resultList.add(demandState);
            }
        }
        return resultList;
    }

    public List<MDemandState> getDemandStateByInterfaceId(String interfaceId) {
        List<MDemandState> resultList = new ArrayList<>();
        for (MDemandState demandState : this.objectMap.values()) {
            if (demandState.isSatisfied() && demandState.getInterfaceId().equals(interfaceId)) {
                resultList.add(demandState);
            }
        }
        return resultList;
    }
}
