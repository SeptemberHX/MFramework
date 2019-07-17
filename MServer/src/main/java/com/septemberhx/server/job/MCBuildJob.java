package com.septemberhx.server.job;

import com.septemberhx.common.bean.MCompositionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MCBuildJob extends MBaseJob {

    private MCompositionRequest compositionRequest;

    public MCBuildJob() {
        this.type = MJobType.CBUILD;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = BUILD;
    }

    public String getImageFullName() {
        return compositionRequest.getDocker_owner() + "/" + compositionRequest.getDocker_name() + ":" + compositionRequest.getDocker_tag();
    }
}
