package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MS2CSetApiCStatus {
    private String instanceId;
    private MApiContinueRequest apiContinueRequest;
}
