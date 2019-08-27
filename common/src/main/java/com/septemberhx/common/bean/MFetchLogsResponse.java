package com.septemberhx.common.bean;

import com.septemberhx.common.log.MServiceBaseLog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MFetchLogsResponse {
    private List<MServiceBaseLog> logList;
}
