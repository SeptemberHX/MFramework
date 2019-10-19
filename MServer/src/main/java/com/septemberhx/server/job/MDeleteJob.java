package com.septemberhx.server.job;

import lombok.Getter;

import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
@Getter
public class MDeleteJob extends MBaseJob {
    private String instanceId;
    private String serviceId;
    private String nodeId;

    public MDeleteJob(String instanceId, String serviceId, String nodeId) {
        this.instanceId = instanceId;
        this.type = MJobType.DELETE;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = DELETE;
        this.serviceId = serviceId;
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "MDeleteJob{" +
                "instanceId='" + instanceId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
