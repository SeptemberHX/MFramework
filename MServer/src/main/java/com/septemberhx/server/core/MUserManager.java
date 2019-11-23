package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.common.base.MDemandChain;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/15
 */
public class MUserManager extends MObjectManager<MUser> {

    public void clear() {
        this.objectMap.clear();
    }

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

    public void verify() {
        for (MUser user : this.getAllValues()) {
            user.verify();
        }
    }
}
