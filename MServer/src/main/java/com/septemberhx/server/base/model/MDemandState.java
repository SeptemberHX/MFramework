package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 *
 * This class stores the state of one user demand.
 * The ID of this class is the ID of the user demand.
 */
@Getter
public class MDemandState extends MBaseObject {
    private String instanceId;  // the id of the service instance that the demand is served at
    private String interfaceId; // the id of the interface this demand is actually served at

    public MDemandState(MUserDemand userDemand) {
        this.id = userDemand.getId();
    }

    public void satisfy(MServiceInstance serviceInstance, MServiceInterface serviceInterface) {
        this.instanceId = serviceInstance.getId();
        this.interfaceId = serviceInterface.getInterfaceId();
    }

    public void clean() {
        this.instanceId = null;
        this.interfaceId = null;
    }

    public boolean isSatisfied() {
        return this.instanceId != null && this.interfaceId != null;
    }
}
