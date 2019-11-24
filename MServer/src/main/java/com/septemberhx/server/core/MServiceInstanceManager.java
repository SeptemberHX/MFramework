package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MServiceInstance;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Setter
public class MServiceInstanceManager extends MObjectManager<MServiceInstance> {

    private static Logger logger = LogManager.getLogger(MServiceInstanceManager.class);
    private Map<String, Set<String>> nodeId2InsIdSet = new HashMap<>();
    private Map<String, Set<String>> serviceId2InsIdSet = new HashMap<>();

    public MServiceInstanceManager shallowClone() {
        MServiceInstanceManager cloneObject = new MServiceInstanceManager();
        Map<String, MServiceInstance> instanceMap = new HashMap<>();  // do not need deep clone here !
        for (MServiceInstance instance : this.objectMap.values()) {
            instanceMap.put(instance.getId(), instance);
        }
        cloneObject.setObjectMap(instanceMap);

        Map<String, Set<String>> cloneNodeId2InsIdSet = new HashMap<>();
        for (String nodeId : this.nodeId2InsIdSet.keySet()) {
            cloneNodeId2InsIdSet.put(nodeId, new HashSet<>(this.nodeId2InsIdSet.get(nodeId)));
        }
        cloneObject.setNodeId2InsIdSet(cloneNodeId2InsIdSet);

        Map<String, Set<String>> cloneServiceId2InsIdSet = new HashMap<>();
        for (String serviceId : this.serviceId2InsIdSet.keySet()) {
            cloneServiceId2InsIdSet.put(serviceId, new HashSet<>(this.serviceId2InsIdSet.get(serviceId)));
        }
        cloneObject.setServiceId2InsIdSet(cloneServiceId2InsIdSet);
        return cloneObject;
    }

    public void delete(String instanceId) {
        if (!this.objectMap.containsKey(instanceId)) {
            return;
        }

        MServiceInstance serviceInstance = this.objectMap.get(instanceId);
        this.objectMap.remove(instanceId);
        this.nodeId2InsIdSet.get(serviceInstance.getNodeId()).remove(instanceId);
        this.serviceId2InsIdSet.get(serviceInstance.getServiceId()).remove(instanceId);
    }

    public void moveInstance(String instanceId, String targetNodeId) {
        if (!this.objectMap.containsKey(instanceId)) {
            return;
        }

        MServiceInstance serviceInstance = this.objectMap.get(instanceId);
        this.nodeId2InsIdSet.get(serviceInstance.getNodeId()).remove(instanceId);
        this.nodeId2InsIdSet.get(targetNodeId).add(instanceId);
        serviceInstance.setNodeId(targetNodeId);
    }

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

    public void add(MServiceInstance serviceInstance) {
        this.objectMap.put(serviceInstance.getId(), serviceInstance);
        if (!this.nodeId2InsIdSet.containsKey(serviceInstance.getNodeId())) {
            this.nodeId2InsIdSet.put(serviceInstance.getNodeId(), new HashSet<>());
        }
        this.nodeId2InsIdSet.get(serviceInstance.getNodeId()).add(serviceInstance.getId());

        if (!this.serviceId2InsIdSet.containsKey(serviceInstance.getServiceId())) {
            this.serviceId2InsIdSet.put(serviceInstance.getServiceId(), new HashSet<>());
        }
        this.serviceId2InsIdSet.get(serviceInstance.getServiceId()).add(serviceInstance.getId());
    }

    public void replace(MServiceInstance serviceInstance) {
        this.objectMap.put(serviceInstance.getId(), serviceInstance);
    }

    public List<MServiceInstance> getInstancesOnNode(String nodeId) {
        List<MServiceInstance> resultList = new ArrayList<>();
        if (this.nodeId2InsIdSet.containsKey(nodeId)) {
            for (String insId : this.nodeId2InsIdSet.get(nodeId)) {
                resultList.add(this.objectMap.get(insId));
            }
        }
        return resultList;
    }

    public List<MServiceInstance> getInstancesOfService(String serviceId) {
        List<MServiceInstance> resultList = new ArrayList<>();
        for (String insId : this.serviceId2InsIdSet.getOrDefault(serviceId, new HashSet<>())) {
            resultList.add(this.objectMap.get(insId));
        }
        return resultList;
    }

    /**
     * Check whether the given objectId is an instance of Gateway.
     * @param objectId: given object id
     * @return Boolean
     */
    public static boolean checkIfInstanceIsGateway(String objectId) {
        return objectId.equalsIgnoreCase("MGateway");
    }
}
