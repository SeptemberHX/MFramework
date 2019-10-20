package com.septemberhx.server.job;

import lombok.Getter;

import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
@Getter
public class MMoveJob extends MBaseJob {
    private String instanceId;
    private String targetNodeId;
    private String rawNodeId;
    private String serviceId;

    public MMoveJob(String instanceId, String targetNodeId, String rawNodeId, String serviceId) {
        this.instanceId = instanceId;
        this.targetNodeId = targetNodeId;
        this.type = MJobType.MOVE;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = MOVE;
        this.rawNodeId = rawNodeId;
        this.serviceId = serviceId;
    }
}
