package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInterface;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/16
 */
public class MServiceManager extends MObjectManager<MService> {

    public MServiceInterface getServiceInterfaceByServiceAndInterfaceId(String serviceId, String interfaceId) {
        if (this.objectMap.containsKey(serviceId)) {
            MService mService = this.objectMap.get(serviceId);
            if (mService.getInterfaceMap().containsKey(interfaceId)) {
                return mService.getInterfaceMap().get(interfaceId);
            }
        }
        return null;
    }
}
