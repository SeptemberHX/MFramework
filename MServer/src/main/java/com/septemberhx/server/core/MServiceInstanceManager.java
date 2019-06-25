package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.MServiceInstance;

import java.util.Optional;


public class MServiceInstanceManager extends MObjectManager<MServiceInstance> {

    public Optional<MServiceInstance> getInstanceByMObjectId(String mObjectId) {
        MServiceInstance result = null;
        for (MServiceInstance instance : this.objectMap.values()) {
            if (instance.getMObjectIdSet().contains(mObjectId)) {
                result = instance;
                break;
            }
        }
        return Optional.of(result);
    }

}
