package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MUser;
import com.septemberhx.server.base.model.MUserDemand;

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
}
