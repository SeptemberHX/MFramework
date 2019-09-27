package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MServiceInterface;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/16
 */
@Setter
public class MServiceManager extends MObjectManager<MService> {

    private Map<String, MServiceInterface> interfaceMap;

    public MServiceInterface getServiceInterfaceByServiceAndInterfaceId(String serviceId, String interfaceId) {
        if (this.objectMap.containsKey(serviceId)) {
            MService mService = this.objectMap.get(serviceId);
            if (mService.getInterfaceMap().containsKey(interfaceId)) {
                return mService.getInterfaceMap().get(interfaceId);
            }
        }
        return null;
    }

    public MServiceManager shallowClone() {
        MServiceManager cloneObject = new MServiceManager();
        Map<String, MService> shallowCloneObjMap = new HashMap<>(this.objectMap);
        Map<String, MServiceInterface> shallowCloneInterfaceMap = new HashMap<>(this.interfaceMap);
        cloneObject.setObjectMap(shallowCloneObjMap);
        cloneObject.setInterfaceMap(shallowCloneInterfaceMap);
        return cloneObject;
    }

    public MServiceInterface getInterfaceById(String interfaceId) {
        return this.interfaceMap.get(interfaceId);
    }

    public void add(MService service) {
        this.objectMap.put(service.getId(), service);
        for (MServiceInterface serviceInterface : service.getAllInterface()) {
            this.interfaceMap.put(serviceInterface.getInterfaceId(), serviceInterface);
        }
    }

    public void remove(String serviceId) {
        if (this.objectMap.containsKey(serviceId)) {
            for (MServiceInterface serviceInterface : this.objectMap.get(serviceId).getAllInterface()) {
                this.interfaceMap.remove(serviceInterface.getInterfaceId());
            }
            this.objectMap.remove(serviceId);
        }
    }

    public List<MService> getAllComServices() {
        return this.objectMap.values().stream().filter(MService::isGenerated).collect(Collectors.toList());
    }

    public List<MServiceInterface> getAllComInterfaces() {
        return this.interfaceMap.values().stream().filter(MServiceInterface::isGenerated).collect(Collectors.toList());
    }
}
