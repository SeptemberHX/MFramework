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
    private String uniqueId;

    public MDeployPodRequest(String id, String nodeId, String uniqueId, V1Pod podBody) {
        this.id = id;
        this.nodeId = nodeId;
        this.podBody = podBody;
        this.uniqueId = uniqueId;
    }

    public MDeployPodRequest() {
    }
}
