package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
public class MFetchLogsBetweenTimeRequest {
    private long startTime;
    private long endTime;
}
