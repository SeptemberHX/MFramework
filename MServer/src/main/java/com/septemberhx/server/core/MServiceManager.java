package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.common.base.MServiceInterface;
import com.septemberhx.common.base.MUserDemand;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/16
 */
@Setter
public class MServiceManager extends MObjectManager<MService> {

    private Map<String, MServiceInterface> interfaceMap = new HashMap<>();

    public MServiceInterface getServiceInterfaceByServiceAndInterfaceId(String serviceId, String interfaceId) {
        if (this.objectMap.containsKey(serviceId)) {
            MService mService = this.objectMap.get(serviceId);
            if (mService.getInterfaceMap().containsKey(interfaceId)) {
                return mService.getInterfaceMap().get(interfaceId);
            }
        }
        return null;
    }

    public void verify() {
        for (MService service : this.getAllValues()) {
            service.verify();
        }
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

    public List<MService> getFixedServiceList() {
        List<MService> resultList = this.getAllValues();
        resultList.sort(new Comparator<MService>() {
            @Override
            public int compare(MService o1, MService o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return resultList;
    }

    public List<MService> getHighRIdOfServiceOrdered(MService service) {
        return this.getAllValues().stream().filter(s ->
                s.getServiceName().equals(service.getServiceName()) && (s.getRId().compareTo(service.getRId()) > 0)
        ).sorted(Comparator.comparing(MService::getRId)).collect(Collectors.toList());
    }

    public List<MService> getAllServicesByServiceName(String serviceName) {
        return this.getAllValues().stream().filter(s -> s.getServiceName().equals(serviceName)).collect(Collectors.toList());
    }

    // for composited services
    public List<MServiceInterface> getCompositedInterfaceStartsWithDemand(MService service, MUserDemand userDemand) {
        List<MServiceInterface> result = new ArrayList<>();

        for (MServiceInterface serviceInterface : service.getAllComInterfaces()) {
            // todo:
        }
        return result;
    }

    public double getMaxInSizeData() {
        double r = 0;
        for (MService service : this.getAllValues()) {
            for (MServiceInterface serviceInterface : service.getAllInterface()) {
                if (serviceInterface.getInDataSize() > r) {
                    r = serviceInterface.getInDataSize();
                }
            }
        }
        return r;
    }

    public double getMaxOutSizeData() {
        double r = 0;
        for (MService service : this.getAllValues()) {
            for (MServiceInterface serviceInterface : service.getAllInterface()) {
                if (serviceInterface.getOutDataSize() > r) {
                    r = serviceInterface.getOutDataSize();
                }
            }
        }
        return r;
    }
}
