package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MDemandChain;
import com.septemberhx.server.base.model.MUser;
import com.septemberhx.server.base.model.MUserDemand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MUserManager extends MObjectManager<MUser> {
    public MUserDemand getUserDemandByUserAndDemandId(String userId, String demandId) {
        if (this.objectMap.containsKey(userId)) {
            MUser mUser = this.objectMap.get(userId);
            MUserDemand mUserDemand = mUser.getDemandByDemandId(demandId);
            if (mUserDemand != null) {
                return mUserDemand;
            }
        }
        return null;
    }

    public void add(MUser user) {
        this.objectMap.put(user.getId(), user);
    }

    public List<MUserDemand> getAllUserDemands() {
        List<MUserDemand> resultList = new ArrayList<>();
        for (MUser user : this.objectMap.values()) {
            for (MDemandChain demandChain : user.getDemandChainList()) {
                resultList.addAll(demandChain.getDemandList());
            }
        }
        return resultList;
    }
}
