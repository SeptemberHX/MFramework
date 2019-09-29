package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MSwitchJob;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
public class MServerOperator extends MObjectManager<MServerState> {

    private Map<String, Integer> insId2LeftCap;     // left user capability of each instance current time
                                                    // For planning only because no action will be done when planning
                                                    // All actions should be done when executing.
    private Map<String, MResource> nodeId2ResourceLeft;
    private Map<String, Set<String>> funcId2InsIdSet;

    // clone objects
    private MDemandStateManager demandStateManager;
    private MServiceInstanceManager instanceManager;
    private MServiceManager serviceManager;

    private List<MBaseJob> jobList;
    private List<MServiceInterface> generatedInterfaceList;     // should update in reInit() and addNewService()
    private Random random = new Random(20190927);

    public MServerOperator() {
        this.insId2LeftCap = new HashMap<>();
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
        this.serviceManager = MSystemModel.getIns().getServiceManager().shallowClone();

        this.jobList.clear();
        this.insId2LeftCap.clear();

        Map<String, Integer> insId2UserNum = new HashMap<>();
        for (MDemandState demandState : MSystemModel.getIns().getDemandStateManager().getAllValues()) {
            if (!insId2UserNum.containsKey(demandState.getInstanceId())) {
                insId2UserNum.put(demandState.getInstanceId(), 0);
            }
            insId2UserNum.put(demandState.getInstanceId(), 1 + insId2UserNum.get(demandState.getInstanceId()));
        }


        for (MServiceInstance instance : this.instanceManager.getAllValues()) {
            Optional<MService> serviceOptional = this.serviceManager.getById(instance.getServiceId());
            serviceOptional.ifPresent(s -> insId2LeftCap.put(instance.getId(), s.getMaxUserCap()));
        }
        for (String instanceId : insId2UserNum.keySet()) {
            Optional<MServiceInstance> instanceOptional = this.instanceManager.getById(instanceId);
            if (instanceOptional.isPresent()) {
                Optional<MService> serviceOptional = this.serviceManager.getById(instanceOptional.get().getServiceId());
                serviceOptional.ifPresent(mService ->
                    insId2LeftCap.put(instanceId, mService.getMaxUserCap() - insId2UserNum.get(instanceId))
                );
            }
        }

        this.funcId2InsIdSet.clear();
        for (MService mService : this.serviceManager.getAllValues()) {
            for (MServiceInterface serviceInterface : mService.getAllInterface()) {
                if (!this.funcId2InsIdSet.containsKey(serviceInterface.getFunctionId())) {
                    this.funcId2InsIdSet.put(serviceInterface.getFunctionId(), new HashSet<>());
                }
                // todo
            }
        }

        this.nodeId2ResourceLeft.clear();
        // get all resources for each node
        for (MServerNode node : MSystemModel.getIns().getMSNManager().getAllValues()) {
            this.nodeId2ResourceLeft.put(node.getId(), node.getResource());
        }
        // and sub the used resources
        for (MServerState serverState : this.objectMap.values()) {
            Optional<MServerNode> nodeOptional = MSystemModel.getIns().getMSNManager().getById(serverState.getId());
            nodeOptional.ifPresent(serverNode ->
                this.nodeId2ResourceLeft.put(serverState.getId(), serverNode.getResource().sub(serverState.getResource()))
            );
        }

        this.generatedInterfaceList = this.serviceManager.getAllComInterfaces();
    }

    public boolean ifNodeHasResForIns(String nodeId, String serviceId) {
        Optional<MService> serviceOptional = this.serviceManager.getById(serviceId);
        if (!serviceOptional.isPresent()) {
            return false;
        }
        MService service = serviceOptional.get();

        return this.nodeId2ResourceLeft.get(nodeId).isEnough(service.getResource());
    }

    public boolean ifInstanceHasCap(String instanceId, Integer capWanted) {
        return this.insId2LeftCap.getOrDefault(instanceId, 0) >= capWanted;
    }

    public MDemandState assignDemandToIns(MUserDemand userDemand, MServiceInstance instance, MDemandState oldState) {
        if (oldState != null) {
            if (this.insId2LeftCap.containsKey(oldState.getInstanceId())) {
                this.insId2LeftCap.put(oldState.getInstanceId(), this.insId2LeftCap.get(oldState.getInstanceId()) + 1);
                this.demandStateManager.deleteById(oldState.getId());
            }
        }
        this.insId2LeftCap.put(instance.getId(), this.insId2LeftCap.getOrDefault(instance.getId(), 0) - 1);
        this.jobList.add(new MSwitchJob(userDemand.getId(), instance.getId()));

        MDemandState newDemandState = this.assignDemandToInterfaceOnSpecificInstance(userDemand, instance);
        this.demandStateManager.add(newDemandState);
        return newDemandState;
    }

    public MServiceInstance addNewInstance(String serviceId, String nodeId, String instanceId) {
        MServiceInstance instance = new MServiceInstance(null, nodeId, null, null, instanceId, null, null, serviceId);
        this.instanceManager.add(instance);
        Optional<MService> serviceOptional = this.serviceManager.getById(serviceId);
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(nodeId).assign(mService.getResource());
            this.insId2LeftCap.put(instance.getId(), mService.getMaxUserCap());
        });
        return instance;
    }

    public void deleteInstance(String instanceId) {
        this.insId2LeftCap.remove(instanceId);
        Optional<MService> serviceOptional = this.serviceManager.getById(instanceId);
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(this.instanceManager.getById(instanceId).get().getNodeId()).free(mService.getResource());
            this.insId2LeftCap.remove(instanceId);
        });
        this.instanceManager.delete(instanceId);
    }

    public List<MServiceInstance> getInstancesOnNode(String nodeId) {
        return new ArrayList<>(this.instanceManager.getInstancesOnNode(nodeId));
    }

    public List<MServiceInstance> getInstancesCanMetWithEnoughCapOnNode(String nodeId, MUserDemand userDemand) {
        List<MServiceInstance> instanceList = this.getInstancesOnNode(nodeId);
        List<MServiceInstance> resultList = new ArrayList<>();
        for (MServiceInstance instance : instanceList) {
            if (!this.ifInstanceHasCap(instance.getId(), 1)) {
                continue;
            }
            // todo: Calculate a functionId2InstanceId map in reInit(). So we don't need to search it in this part
            Optional<MService> serviceOptional = this.serviceManager.getById(instance.getServiceId());
            serviceOptional.ifPresent(mService -> {
                if (mService.getInterfaceMetUserDemand(userDemand).size() > 0) {
                    resultList.add(instance);
                }
            });
        }
        return resultList;
    }

    public List<MService> getAllSatisfiedService(MUserDemand userDemand) {
        List<MService> allServices = this.serviceManager.getAllValues();
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
        Optional<MService> serviceOptional = this.serviceManager.getById(serviceInstance.getServiceId());
        MDemandState state = null;
        if (serviceOptional.isPresent()) {
            List<MServiceInterface> interfaceList = serviceOptional.get().getInterfaceMetUserDemand(userDemand);
            state = new MDemandState(userDemand);
            state.satisfy(serviceInstance, interfaceList.get(this.random.nextInt(interfaceList.size())));
        }
        return state;
    }

    public MService findBestServiceToCreate(MUserDemand userDemand) {
        List<MService> serviceList = this.getAllSatisfiedService(userDemand);
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

    public void addNewService(MService service) {
        this.serviceManager.add(service);
        this.generatedInterfaceList = this.serviceManager.getAllComInterfaces();
    }

    /**
     * This function will try to match the longest composited service from the startIndex
     * The matched service and next index of the matched sub-demands will be returned
     * @param userDemands: given demand chain
     * @param startIndex: start index
     * @return pair of matched service and the next index of the matched sub-demands
     */
    public Pair<MServiceInterface, Integer> findNextSuitableComService(List<MUserDemand> userDemands, int startIndex) {
        int maxLength = 1;
        MServiceInterface targetInterface = null;
        if (startIndex < userDemands.size()) {
            for (MServiceInterface serviceInterface : this.generatedInterfaceList) {
                List<String> interfaceIdList = serviceInterface.getCompositionList();
                boolean ifSuccess = true;
                for (int i = startIndex, j = 0; i < userDemands.size() && j < interfaceIdList.size(); ++i, ++j) {
                    MServiceInterface subInterface = this.serviceManager.getInterfaceById(interfaceIdList.get(j));
                    if (!userDemands.get(i).isServiceInterfaceMet(subInterface)) {
                        ifSuccess = false;
                        break;
                    }
                }

                if (ifSuccess) {
                    if (serviceInterface.getCompositionList().size() >= maxLength) {
                        maxLength = serviceInterface.getCompositionList().size();
                        targetInterface = serviceInterface;
                    }
                }
            }
        }
        return new Pair<>(targetInterface, startIndex + maxLength);
    }

    MResource getNodeLeftResource(String nodeId) {
        return this.nodeId2ResourceLeft.get(nodeId);
    }

    Integer getInstanceLeftCap(String instanceId) {
        return this.insId2LeftCap.getOrDefault(instanceId, 0);
    }

    MServiceInstance getInstanceById(String instanceId) {
        return this.instanceManager.getById(instanceId).orElse(null);
    }

    MService getServiceById(String serviceId) {
        return this.serviceManager.getById(serviceId).orElse(null);
    }
}
