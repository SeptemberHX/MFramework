package com.septemberhx.server.job;

import com.septemberhx.common.bean.MDeployNotifyRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MDeployJobResult extends MBaseJobResult {
    private String instanceId;

    public MDeployJobResult(MDeployNotifyRequest deployNotifyRequest) {
        this.jobId = deployNotifyRequest.getId();
        this.instanceId = deployNotifyRequest.getInstanceId();
        this.type = MJobType.DEPLOY;
    }
}
