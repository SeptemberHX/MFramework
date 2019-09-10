package com.septemberhx.common.bean;

import io.kubernetes.client.models.V1Pod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MDeployPodRequest {
    private String id;
    private String nodeId;
    private V1Pod podBody;

    public MDeployPodRequest(String id, String nodeId, V1Pod podBody) {
        this.id = id;
        this.nodeId = nodeId;
        this.podBody = podBody;
    }

    public MDeployPodRequest() {
    }
}
