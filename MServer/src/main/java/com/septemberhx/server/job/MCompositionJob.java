package com.septemberhx.server.job;

import com.septemberhx.common.bean.MCompositionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MCompositionJob extends MBaseJob {

    private MCompositionRequest compositionRequest;

    public MCompositionJob() {
        this.type = MJobType.COMPOSITE;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
    }
}
