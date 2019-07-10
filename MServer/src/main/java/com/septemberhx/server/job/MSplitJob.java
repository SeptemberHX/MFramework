package com.septemberhx.server.job;

import com.septemberhx.server.base.MClassFunctionPair;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MSplitJob extends MBaseJob {
    private String instanceId;
    private String parentMObjectId;
    private MClassFunctionPair breakPoint;
    private MClassFunctionPair breakBody;
    public MSplitJob() {
        this.type = MJobType.SPLIT;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
    }
}
