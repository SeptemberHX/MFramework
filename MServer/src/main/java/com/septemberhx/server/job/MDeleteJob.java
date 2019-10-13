package com.septemberhx.server.job;

import lombok.Getter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
@Getter
public class MDeleteJob extends MBaseJob {
    private String instanceId;

    public MDeleteJob(String instanceId) {
        this.instanceId = instanceId;
        this.type = MJobType.DELETE;
        this.priority = DELETE;
    }

    @Override
    public String toString() {
        return "MDeleteJob{" +
                "instanceId='" + instanceId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
