package com.septemberhx.server.job;

import com.septemberhx.server.utils.MIDUtils;
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
    private String oldInstanceId;

    public MSwitchJob(String userDemandId, String instanceId, String oldInstanceId) {
        this.type = MJobType.SWITCH;
        this.userDemandId = userDemandId;
        this.instanceId = instanceId;
        this.oldInstanceId = oldInstanceId;
    }

    @Override
    public double cost() {
        if (MIDUtils.getNodeIdFromInstanceId(oldInstanceId).equals(MIDUtils.getNodeIdFromInstanceId(instanceId))) {
            return 0.0;
        } else {
            // todo: set MSwitch cost
            return 1.0;
        }
    }

    @Override
    public String toString() {
        return "MSwitchJob{" +
                "userDemandId='" + userDemandId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", oldInstanceId='" + oldInstanceId + '\'' +
                '}';
    }
}
