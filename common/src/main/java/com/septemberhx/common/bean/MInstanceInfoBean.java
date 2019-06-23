package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * MInstanceInfoBean
 *
 * @author septemberhx
 * @date 2019-06-15
 */

@Getter
@Setter
@ToString
public class MInstanceInfoBean implements Serializable {
    private String id;
    private String ip;
    private int port;
    /**
     * to build the topology
     */
    private Map<String, String> parentIdMap;
    private Map<String, Set<String>> apiMap;
    private MDockerInfoBean dockerInfo;
}
