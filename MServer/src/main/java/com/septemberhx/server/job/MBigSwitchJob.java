package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/25
 */
@Getter
@Setter
public class MBigSwitchJob extends MBaseJob {
    private List<MBaseJob> switchJobList;

    public MBigSwitchJob() {
        this.type = MJobType.BIGSWITCH;
        this.id = this.type + "_" + UUID.randomUUID().toString();
    }
}
