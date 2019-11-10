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
public class MDemandState extends MBaseObject {
    private String instanceId;  // the id of the service instance that the demand is served at
    private String interfaceId; // the id of the interface this demand is actually served at
    private String userId;      // the id of the user
    private String nodeId;      // the id of the node where the instance located

    private String comAssignId;

    public MDemandState(MUserDemand userDemand) {
        this.id = userDemand.getId();
        this.userId = userDemand.getUserId();
    }

    public void satisfy(MServiceInstance serviceInstance, MServiceInterface serviceInterface) {
        this.instanceId = serviceInstance.getId();
        this.interfaceId = serviceInterface.getInterfaceId();
        this.nodeId = serviceInstance.getNodeId();
    }

    public void compSatisfy(MServiceInstance serviceInstance, MServiceInterface serviceInterface, String comAssignId) {
        this.satisfy(serviceInstance, serviceInterface);
        this.comAssignId = comAssignId;
    }

    private void clean() {
        this.comAssignId = null;
        this.instanceId = null;
        this.interfaceId = null;
        this.nodeId = null;
    }

    public boolean isAssigned() {
        return this.instanceId != null && this.interfaceId != null && this.nodeId != null;
    }

    @Override
    public String toString() {
        return "MDemandState{" +
                "instanceId='" + instanceId + '\'' +
                ", interfaceId='" + interfaceId + '\'' +
                ", userId='" + userId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", comAssignId='" + comAssignId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    private MDemandState() {
        this.clean();
    }

    public MDemandState deepClone() {
        MDemandState r = new MDemandState();
        r.id = this.id;
        r.userId = this.userId;
        r.instanceId = this.instanceId;
        r.interfaceId = this.interfaceId;
        r.nodeId = this.nodeId;
        r.comAssignId = this.comAssignId;
        return r;
    }

    public MDemandState(String id, String instanceId, String interfaceId, String userId, String nodeId) {
        this.id = id;
        this.instanceId = instanceId;
        this.interfaceId = interfaceId;
        this.userId = userId;
        this.nodeId = nodeId;
        this.comAssignId = null;
    }

    public MDemandState(String id, String instanceId, String interfaceId, String userId, String nodeId, String comAssignId) {
        this(id, instanceId, interfaceId, userId, nodeId);
        this.comAssignId = comAssignId;
    }

    public boolean isAssignAsComp() {
        return this.comAssignId != null;
    }
}
