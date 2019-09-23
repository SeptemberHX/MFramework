package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
@Getter
@Setter
public class MSwitchJob extends MBaseJob {
    private String userDemandId;
    private String instanceId;

    public MSwitchJob(String userDemandId, String instanceId) {
        this.type = MJobType.SWITCH;
        this.userDemandId = userDemandId;
        this.instanceId = instanceId;
    }
}
