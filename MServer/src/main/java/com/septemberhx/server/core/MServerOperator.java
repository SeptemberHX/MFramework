package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MSwitchJob;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
public class MServerOperator extends MObjectManager<MServerState> {

    private Map<String, Integer> insId2UserCap;     // left user capability of each instance current time
                                                    // For planning only because no action will be done when planning
                                                    // All actions should be done when executing.
    private Map<String, MResource> nodeId2ResourceLeft;
    private Map<String, Set<String>> funcId2InsIdSet;

    // clone objects
    private MDemandStateManager demandStateManager;
    private MServiceInstanceManager instanceManager;

    private List<MBaseJob> jobList;
    private Random random = new Random(20190927);

    public MServerOperator() {
        this.insId2UserCap = new HashMap<>();
        this.nodeId2ResourceLeft = new HashMap<>();
        this.funcId2InsIdSet = new HashMap<>();
        this.jobList = new ArrayList<>();
    }

    /**
     * This function will init all member variables which are used by other functions.
     * SHOULD BE CALLED BEFORE ANY OTHER OPERATIONS.
     */
    public void reInit() {
        this.demandStateManager = MSystemModel.getIns().getDemandStateManager().shallowClone();
        this.instanceManager = MSystemModel.getIns().getMSIManager().shallowClone();

        this.jobList.clear();
        this.insId2UserCap.clear();

        Map<String, Integer> insId2UserNum = new HashMap<>();
        for (MDemandState demandState : MSystemModel.getIns().getDemandStateManager().getAllValues()) {
            if (!insId2UserNum.containsKey(demandState.getInstanceId())) {
                insId2UserNum.put(demandState.getInstanceId(), 0);
            }
            insId2UserNum.put(demandState.getInstanceId(), 1 + insId2UserNum.get(demandState.getInstanceId()));
        }

        for (String instanceId : insId2UserNum.keySet()) {
            Optional<MServiceInstance> instanceOptional = MSystemModel.getIns().getInstanceById(instanceId);
            if (instanceOptional.isPresent()) {
                Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(instanceOptional.get().getServiceId());
                serviceOptional.ifPresent(mService ->
                    insId2UserCap.put(instanceId, mService.getMaxUserCap() - insId2UserCap.get(instanceId))
                );
            }
        }

        this.funcId2InsIdSet.clear();
        for (MService mService : MSystemModel.getIns().getServiceManager().getAllValues()) {
            for (MServiceInterface serviceInterface : mService.getAllInterface()) {
                if (!this.funcId2InsIdSet.containsKey(serviceInterface.getFunctionId())) {
                    this.funcId2InsIdSet.put(serviceInterface.getFunctionId(), new HashSet<>());
                }
                // todo
            }
        }

        this.nodeId2ResourceLeft.clear();
        for (MServerState serverState : this.objectMap.values()) {
            Optional<MServerNode> nodeOptional = MSystemModel.getIns().getMSNManager().getById(serverState.getId());
            nodeOptional.ifPresent(serverNode ->
                this.nodeId2ResourceLeft.put(serverState.getId(), serverNode.getResource().sub(serverState.getResource()))
            );
        }
    }

    public boolean ifNodeHasResForIns(String nodeId, String serviceId) {
        Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(serviceId);
        if (!serviceOptional.isPresent()) {
            return false;
        }
        MService service = serviceOptional.get();

        return this.nodeId2ResourceLeft.get(nodeId).isEnough(service.getResource());
    }

    public boolean ifInstanceHasCap(String instanceId, Integer capWanted) {
        return this.insId2UserCap.getOrDefault(instanceId, 0) >= capWanted;
    }

    public void assignDemandToIns(MUserDemand userDemand, MServiceInstance instance, MDemandState oldState) {
        if (oldState != null) {
            if (!this.insId2UserCap.containsKey(oldState.getInstanceId())) {
                this.insId2UserCap.put(oldState.getInstanceId(), this.insId2UserCap.get(oldState.getInstanceId()) - 1);
                this.demandStateManager.deleteById(oldState.getId());
            }
        }
        this.insId2UserCap.put(instance.getId(), 1 + this.insId2UserCap.getOrDefault(instance.getId(), 0));
        this.jobList.add(new MSwitchJob(userDemand.getId(), instance.getId()));

        MDemandState newDemandState = this.assignDemandToInterfaceOnSpecificInstance(userDemand, instance);
        this.demandStateManager.add(newDemandState);
    }

    public MServiceInstance addNewInstance(String serviceId, String nodeId, String instanceId) {
        MServiceInstance instance = new MServiceInstance(null, nodeId, null, null, instanceId, null, null, serviceId);
        this.instanceManager.add(instance);
        Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(serviceId);
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(nodeId).assign(mService.getResource());
            this.insId2UserCap.put(instance.getId(), mService.getMaxUserCap());
        });
        return instance;
    }

    public void deleteInstance(MServiceInstance serviceInstance) {
        this.insId2UserCap.remove(serviceInstance.getId());
        Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(serviceInstance.getServiceId());
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(serviceInstance.getNodeId()).free(mService.getResource());
            this.insId2UserCap.remove(serviceInstance.getId());
        });
        this.instanceManager.delete(serviceInstance);
    }

    public List<MServiceInstance> getInstancesOnNode(String nodeId) {
        return new ArrayList<>(this.instanceManager.getInstancesOnNode(nodeId));
    }

    public List<MServiceInstance> getInstancesCanMetWithEnoughCapOnNode(String nodeId, MUserDemand userDemand) {
        List<MServiceInstance> instanceList = this.getInstancesOnNode(nodeId);
        for (MServiceInstance instance : instanceList) {
            if (!this.ifInstanceHasCap(instance.getId(), 1)) {
                continue;
            }
            // todo: Calculate a functionId2InstanceId map in reInit(). So we don't need to search it in this part
            Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(instance.getServiceId());
            serviceOptional.ifPresent(mService -> {
                if (mService.getInterfaceMetUserDemand(userDemand).size() > 0) {
                    instanceList.add(instance);
                }
            });
        }
        return instanceList;
    }

    public List<MService> getAllSatisfiedService(MUserDemand userDemand) {
        List<MService> allServices = MSystemModel.getIns().getServiceManager().getAllValues();
        return allServices.stream().filter(s -> s.checkIfMeetDemand(userDemand)).collect(Collectors.toList());
    }

    /**
     * This function will be called after the demand is decided to be dispatched to which instance
     * Considering maybe there are two or more interfaces can satisfy users, we will assign the demand to any of them
     * Because the resource usage is the constraint of the instance, not the interface
     * @param userDemand: user demand
     * @param serviceInstance: the given instance
     */
    private MDemandState assignDemandToInterfaceOnSpecificInstance(MUserDemand userDemand, MServiceInstance serviceInstance) {
        Optional<MService> serviceOptional = MSystemModel.getIns().getServiceManager().getById(serviceInstance.getServiceId());
        MDemandState state = null;
        if (serviceOptional.isPresent()) {
            List<MServiceInterface> interfaceList = serviceOptional.get().getInterfaceMetUserDemand(userDemand);
            state = new MDemandState(userDemand);
            state.satisfy(serviceInstance, interfaceList.get(this.random.nextInt(interfaceList.size())));
        }
        return state;
    }

    public MService findBestServiceToCreate(MUserDemand userDemand) {
        List<MService> serviceList = MSystemModel.getIns().getServiceManager().getAllValues();
        if (serviceList.isEmpty()) {
            logger.warn("User demand cannot find suitable service: " + userDemand.toString());
            return null;
        }
        MService bestService = serviceList.get(0);
        int maxServiceInstanceNum = this.instanceManager.getInstancesOfService(bestService.getId()).size();
        for (MService service : serviceList) {
            int tNum = this.instanceManager.getInstancesOfService(service.getId()).size();
            if (tNum > maxServiceInstanceNum) {
                maxServiceInstanceNum = tNum;
                bestService = service;
            }
        }
        return bestService;
    }
}
