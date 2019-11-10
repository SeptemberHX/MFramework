package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public class MUser extends MBaseObject {
    private List<MDemandChain> demandChainList;      // user demand chain to satisfy user AT THIS TIME ONLY
    private MPosition position;

    public MUserDemand getDemandByDemandId(String demandId) {
        for (MDemandChain demandChain : this.demandChainList) {
            if (demandChain.containsDemandId(demandId)) {
                return demandChain.getDemandById(demandId);
            }
        }
        return null;
    }

    public List<MUserDemand> getAllDemands() {
        List<MUserDemand> r = new ArrayList<>();
        for (MDemandChain demandChain : this.demandChainList) {
            List<MUserDemand> demandList = demandChain.getDemandList();
            r.addAll(demandList);
        }
        return r;
    }

    @Override
    public String toString() {
        return "MUser{" +
                "demandChainList=" + demandChainList +
                ", position=" + position +
                ", id='" + id + '\'' +
                '}';
    }

    public void verify() {
        for (MDemandChain chain : this.demandChainList) {
            if (!chain.verify()) {
                throw new RuntimeException("%s failed to verify demand chain list");
            }

            for (MUserDemand demand : chain.getDemandList()) {
                if (!demand.getUserId().equals(this.id)) {
                    throw new RuntimeException(
                            String.format("%s|%s failed to be verified", this.id, demand.getId())
                    );
                }
            }
        }
    }

    public MDemandChain getContainedChain(String demandId) {
        for (MDemandChain demandChain : this.getDemandChainList()) {
            if (demandChain.containsDemandId(demandId)) {
                return demandChain;
            }
        }

        return null;
    }
}
