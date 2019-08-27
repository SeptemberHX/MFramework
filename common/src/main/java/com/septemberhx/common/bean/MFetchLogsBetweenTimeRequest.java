package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MFetchLogsBetweenTimeRequest {
    private Date startTime;
    private Date endTime;
}
