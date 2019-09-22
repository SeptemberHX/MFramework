package com.septemberhx.server.base.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/22
 */
public class MDemandChain {

    private List<String> demandIdList;          // This stands for a intact demand chains
    private Map<String, MUserDemand> demandMap; // Store the demands

    public MDemandChain() {
        this.demandIdList = new ArrayList<>();
        this.demandMap = new HashMap<>();
    }

    public void addDemand(MUserDemand userDemand) {
        this.demandMap.put(userDemand.getId(), userDemand);
        this.demandIdList.add(userDemand.getId());
    }

    public void addDemandList(List<MUserDemand> demandList) {
        for (MUserDemand userDemand : demandList) {
            this.addDemand(userDemand);
        }
    }

    public boolean containsDemandId(String demandId) {
        return this.demandMap.containsKey(demandId);
    }

    public MUserDemand getDemandById(String demandId) {
        return this.demandMap.getOrDefault(demandId, null);
    }
}
