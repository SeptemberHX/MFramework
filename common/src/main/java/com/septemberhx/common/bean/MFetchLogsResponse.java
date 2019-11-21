package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MFetchLogsResponse {
    private List<String> logList;
}
