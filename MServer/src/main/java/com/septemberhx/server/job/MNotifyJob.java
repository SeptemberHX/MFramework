package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class MNotifyJob extends MBaseJob {
    public MNotifyJob() {
        type = MJobType.NOTIFY;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
    }
}
