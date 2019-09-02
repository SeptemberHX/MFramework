package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
public class MFetchLogsBetweenTimeRequest {
    private DateTime startTime;
    private DateTime endTime;
}