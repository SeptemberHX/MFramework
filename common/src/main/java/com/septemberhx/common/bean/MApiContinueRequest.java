package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MApiContinueRequest {
    private String objectId;
    private String functionName;
    private Boolean status;
}
