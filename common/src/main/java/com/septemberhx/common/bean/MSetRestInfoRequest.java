package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MSetRestInfoRequest {
    private MInstanceRestInfoBean restInfoBean;
    private String instanceId;
}
