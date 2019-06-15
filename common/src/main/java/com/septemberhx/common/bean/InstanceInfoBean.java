package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@ToString
public class InstanceInfoBean implements Serializable {

    private String ip;  // to identify the pod it belongs to
    private Map<String, String> parentIdMap;  // to build the topology
}
