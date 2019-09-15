package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


public class MServiceInstanceManager extends MObjectManager<MServiceInstance> {

    private static Logger logger = LogManager.getLogger(MServiceInstanceManager.class);

    public Optional<MServiceInstance> getInstanceByMObjectId(String mObjectId) {
        MServiceInstance result = null;
        for (MServiceInstance instance : this.objectMap.values()) {
            if (instance.getMObjectIdSet().contains(mObjectId)) {
                result = instance;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public Optional<MServiceInstance> getInstanceByIpAddr(String ipAddr) {
        MServiceInstance result = null;
        for (MServiceInstance instance : this.objectMap.values()) {
            if (instance.getIp().equals(ipAddr)) {
                result = instance;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    /**
     * Check whether the given ip address is an instance of Gateway.
     * @param ipAddr: The ip address of the given instance
     * @return Boolean
     */
    public static boolean checkIfInstanceIsGateway(String ipAddr) {
        Optional<MServiceInstance> serviceInstanceOptional =
                MSystemModel.getInstance().getMSIManager().getInstanceByIpAddr(ipAddr);
        if (!serviceInstanceOptional.isPresent()) {
            logger.warn("The log came from an nonexistent instance : " + ipAddr);
            return false;
        }

        MServiceInstance serviceInstance = serviceInstanceOptional.get();
        return MService.checkIfInstanceIsGatewayByServiceName(serviceInstance.getServiceName());
    }
}
