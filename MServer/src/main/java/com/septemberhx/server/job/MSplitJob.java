package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MSplitJob extends MBaseJob {
    public MSplitJob() {
        this.type = MJobType.SPLIT;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
    }
}
