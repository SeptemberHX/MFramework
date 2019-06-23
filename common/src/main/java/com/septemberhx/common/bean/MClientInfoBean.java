package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class MClientInfoBean {
    private Map<String, Set<String>> apiMap;
    private Map<String, String> parentIdMap;
    private MDockerInfoBean dockerInfoBean;
}
