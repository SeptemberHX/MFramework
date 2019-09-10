package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MApiContinueRequest {
    private List<MApiSplitBean> splitBeans;
}
