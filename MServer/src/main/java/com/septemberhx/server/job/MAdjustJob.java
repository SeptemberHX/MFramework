package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/19
 */
@Getter
@Setter
public class MAdjustJob extends MBaseJob {
    private String instanceId;
    private String targetRId;
    private String rawServiceId;
    private String targetServiceId;
    private String nodeId;

    public MAdjustJob(String instanceId, String targetRId, String rawServiceId, String targetServiceId, String nodeId) {
        this.instanceId = instanceId;
        this.type = MJobType.ADJUST;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = ADJUST;
        this.targetRId = targetRId;
        this.rawServiceId = rawServiceId;
        this.targetServiceId = targetServiceId;
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "MAdjustJob{" +
                "instanceId='" + instanceId + '\'' +
                ", targetRId='" + targetRId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
