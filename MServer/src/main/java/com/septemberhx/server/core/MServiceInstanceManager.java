package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.Option;
import java.util.*;

@Setter
public class MServiceInstanceManager extends MObjectManager<MServiceInstance> {

    private static Logger logger = LogManager.getLogger(MServiceInstanceManager.class);
    private Map<String, Set<String>> nodeId2InsIdSet = new HashMap<>();

    public MServiceInstanceManager shallowClone() {
        MServiceInstanceManager cloneObejct = new MServiceInstanceManager();
        Map<String, MServiceInstance> instanceMap = new HashMap<>(this.objectMap);  // do not need deep clone here !
        Map<String, Set<String>> cloneMapSet = new HashMap<>();
        for (String nodeId : this.nodeId2InsIdSet.keySet()) {
            cloneMapSet.put(nodeId, new HashSet<>(this.nodeId2InsIdSet.get(nodeId)));
        }
        cloneObejct.setNodeId2InsIdSet(nodeId2InsIdSet);
        cloneObejct.setObjectMap(instanceMap);
        return cloneObejct;
    }

    public void delete(MServiceInstance serviceInstance) {
        this.objectMap.remove(serviceInstance.getId());
        this.nodeId2InsIdSet.get(serviceInstance.getNodeId()).remove(serviceInstance.getId());
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
