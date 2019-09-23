package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

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
}
