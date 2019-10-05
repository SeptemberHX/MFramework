package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 *
 * This class stores the state of one user demand.
 * The ID of this class is the ID of the user demand.
 */
@Getter
@Setter
public class MDemandState extends MBaseObject {
    private String instanceId;  // the id of the service instance that the demand is served at
    private String interfaceId; // the id of the interface this demand is actually served at
    private String userId;      // the id of the user
    private String nodeId;      // the id of the node where the instance located

    public MDemandState(MUserDemand userDemand) {
        this.id = userDemand.getId();
        this.userId = userDemand.getUserId();
    }

    public void satisfy(MServiceInstance serviceInstance, MServiceInterface serviceInterface) {
        this.instanceId = serviceInstance.getId();
        this.interfaceId = serviceInterface.getInterfaceId();
        this.nodeId = serviceInstance.getNodeId();
    }

    public void clean() {
        this.instanceId = null;
        this.interfaceId = null;
        this.nodeId = null;
    }

    public boolean isAssigned() {
        return this.instanceId != null && this.interfaceId != null && this.nodeId != null;
    }
}
