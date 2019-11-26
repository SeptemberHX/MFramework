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
    private String uniqueId;
    private String serviceName;
    private String imageUrl;

    public MDeployPodRequest(String id, String nodeId, String uniqueId, String serviceName, String imageUrl) {
        this.id = id;
        this.nodeId = nodeId;
        this.uniqueId = uniqueId;
        this.serviceName = serviceName;
        this.imageUrl = imageUrl;
    }

    public MDeployPodRequest() {
    }
}
