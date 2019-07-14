package com.septemberhx.server.job;

import com.septemberhx.common.bean.MDeployPodRequest;
import io.kubernetes.client.models.V1Pod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class MDeployJob extends MBaseJob {
    private String nodeId;
    private String imageName;
    private V1Pod pod;

    public MDeployJob() {
        type = MJobType.DEPLOY;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = DEPLOY;
    }

    public MDeployPodRequest toMDeployPodRequest() {
        if (pod.getSpec().getContainers().size() > 0) {
            pod.getSpec().getContainers().get(0).setImage(imageName);
        }
        return new MDeployPodRequest(id, nodeId, pod);
    }
}
