package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/12/4
 */
@Getter
@Setter
public class MSystemInfoBean {
    private Integer totalDemandNum;
    private Integer totalDemandServiceNum;
    private Integer totalDemandKindNum;
    private Map<String, Double> nodeCpuUsagePercentMap = new HashMap<>();
    private Map<String, Double> nodeRamUsagePercentMap = new HashMap<>();

    @Override
    public String toString() {
        return "MSystemInfoBean{" +
                "totalDemandNum=" + totalDemandNum +
                ", totalDemandServiceNum=" + totalDemandServiceNum +
                ", totalDemandKindNum=" + totalDemandKindNum +
                ", nodeCpuUsagePercentMap=" + nodeCpuUsagePercentMap +
                ", nodeRamUsagePercentMap=" + nodeRamUsagePercentMap +
                '}';
    }
}
