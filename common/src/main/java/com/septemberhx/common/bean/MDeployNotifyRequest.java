package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MDeployNotifyRequest {

    public enum DeployStatus {
        SUCCESS,
        FAILED
    };

    private String id;
    private DeployStatus status;
    private String instanceId;

    public MDeployNotifyRequest(String id, String instanceId) {
        this.id = id;
        this.status = DeployStatus.SUCCESS;
        this.instanceId = instanceId;
    }

    public MDeployNotifyRequest() {
    }
}
