package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public abstract class MBaseGA {
    static List<String> fixedNodeIdList;
    static List<String> fixedServiceIdList;
    static Map<String, Integer> fixedNodeId2Index;
    static Map<String, Integer> fixedServiceId2Index;

    MPopulation population;

    public MBaseGA(MServerOperator snapshotOperator) {
        fixedNodeIdList = new ArrayList<>();
        fixedServiceIdList = new ArrayList<>();
        MSystemModel.getIns().getMSNManager().getFixedOrderNodeList().forEach(n -> fixedNodeIdList.add(n.getId()));
        snapshotOperator.getServiceManager().getFixedServiceList().forEach(s -> fixedServiceIdList.add(s.getId()));

        fixedNodeId2Index = new HashMap<>();
        for (int i = 0; i < fixedNodeIdList.size(); ++i) {
            fixedNodeId2Index.put(fixedNodeIdList.get(i), i);
        }

        fixedServiceId2Index = new HashMap<>();
        for (int i = 0; i < fixedServiceIdList.size(); ++i) {
            fixedServiceId2Index.put(fixedServiceIdList.get(i), i);
        }
    }

    public abstract void evolve();
}
