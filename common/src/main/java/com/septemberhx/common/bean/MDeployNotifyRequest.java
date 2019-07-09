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

    public MDeployNotifyRequest(String id, DeployStatus status) {
        this.id = id;
        this.status = status;
    }

    public MDeployNotifyRequest() {
    }
}
