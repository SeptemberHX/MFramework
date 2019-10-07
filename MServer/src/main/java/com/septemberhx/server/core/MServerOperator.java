package com.septemberhx.server.core;

import com.septemberhx.common.base.MArchitectInfo;
import com.septemberhx.common.base.MClassFunctionPair;
import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.common.bean.MCompositionRequest;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MCBuildJob;
import com.septemberhx.server.job.MSwitchJob;
import com.septemberhx.server.utils.MIDUtils;
import lombok.Getter;
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

    @Getter
    private MServiceManager serviceManager;

    @Getter
    private List<MBaseJob> jobList;
    private List<MServiceInterface> generatedInterfaceList;     // should update in reInit() and addNewService()
    private static Random random = new Random(20190927);

    public MServerOperator() {
        this.insId2LeftCap = new HashMap<>();
        this.nodeId2ResourceLeft = new HashMap<>();
        this.funcId2InsIdSet = new HashMap<>();
        this.jobList = new ArrayList<>();
    }

    public MServerOperator shallowClone() {
        MServerOperator operator = new MServerOperator();
        Map<String, MServerState> objMapClone = new HashMap<>();
        for (String nodeId : this.objectMap.keySet()) {
            objMapClone.put(nodeId, this.objectMap.get(nodeId).mclone());
        }
        operator.objectMap = objMapClone;

        operator.insId2LeftCap = new HashMap<>(this.insId2LeftCap);
        Map<String, MResource> nodeId2ResourceLeftClone = new HashMap<>();
        for (String nodeId : this.nodeId2ResourceLeft.keySet()) {
            nodeId2ResourceLeftClone.put(nodeId, new MResource(this.nodeId2ResourceLeft.get(nodeId)));
        }
        operator.nodeId2ResourceLeft = nodeId2ResourceLeftClone;

        Map<String, Set<String>> funcId2InsIdSetClone = new HashMap<>();
        for (String funcId : this.funcId2InsIdSet.keySet()) {
            funcId2InsIdSetClone.put(funcId, new HashSet<>(this.funcId2InsIdSet.get(funcId)));
        }
        operator.funcId2InsIdSet = funcId2InsIdSetClone;

        operator.demandStateManager = this.demandStateManager.shallowClone();
        operator.instanceManager = this.instanceManager.shallowClone();
        operator.serviceManager = this.serviceManager.shallowClone();
        operator.jobList = new ArrayList<>(this.jobList);
        operator.generatedInterfaceList = new ArrayList<>(this.generatedInterfaceList);
        return operator;
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

    public List<MUserDemand> deleteInstance(String instanceId) {
        // collect user demands on this instance
        List<MUserDemand> userDemands = new ArrayList<>();
        for (MDemandState demandState : this.demandStateManager.getDemandStateByInstanceId(instanceId)) {
            userDemands.add(
                    MSystemModel.getIns()
                            .getUserManager()
                            .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getId()));
        }

        // remove instance data
        this.insId2LeftCap.remove(instanceId);
        Optional<MService> serviceOptional = this.serviceManager.getById(instanceId);
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(this.instanceManager.getById(instanceId).get().getNodeId()).free(mService.getResource());
            this.insId2LeftCap.remove(instanceId);
        });
        this.instanceManager.delete(instanceId);

        return userDemands;
    }

    public boolean moveInstance(String instanceId, String targetNodeId) {
        Optional<MServiceInstance> instanceOptional = this.instanceManager.getById(instanceId);
        if (!instanceOptional.isPresent()) return false;
        MServiceInstance instance = instanceOptional.get();
        if (instance.getNodeId().equals(targetNodeId)) return true;

        // change instance location
        Optional<MService> serviceOptional = this.serviceManager.getById(instanceId);
        if (!serviceOptional.isPresent()) return false;

        if (this.nodeId2ResourceLeft.get(targetNodeId).isEnough(serviceOptional.get().getResource())) {
            this.nodeId2ResourceLeft.get(instance.getNodeId()).free(serviceOptional.get().getResource());
            this.nodeId2ResourceLeft.get(targetNodeId).assign(serviceOptional.get().getResource());
            this.instanceManager.moveInstance(instanceId, targetNodeId);
        }

        // change user demands on this instance
        for (MDemandState demandState : this.demandStateManager.getDemandStateByInstanceId(instanceId)) {
            demandState.setNodeId(targetNodeId);
        }
        return true;
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

    public void addNewJob(MBaseJob baseJob) {
        this.jobList.add(baseJob);
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

    // ------------------- Composition Part --------------------
    public void compositeService(MService service1, MServiceInterface interface1, MService service2, MServiceInterface interface2) {
        // interfaceId contains serviceName
        String serviceId = MIDUtils.generateServiceId(String.format("%s__%s", interface1.getInterfaceId(), interface2.getInterfaceId()));
        String serviceName = serviceId;
        String functionName = String.format("%s__%s", interface1.getFullFuncName(), interface2.getFullFuncName());
        MServiceInterface newInterface = new MServiceInterface();
        newInterface.setInterfaceId(MIDUtils.generateInterfaceId(serviceId, functionName));
        newInterface.setSlaLevel(-1);
        newInterface.setFunctionId(MIDUtils.generateFunctionId(functionName));
        newInterface.setServiceId(serviceId);

        List<String> compositionList = new ArrayList<>();
        compositionList.addAll(interface1.getCompositionList());
        compositionList.addAll(interface2.getCompositionList());
        newInterface.setCompositionList(compositionList);

        Map<String, MServiceInterface> interfaceMap = new HashMap<>();
        interfaceMap.put(newInterface.getInterfaceId(), newInterface);

        MService newService = new MService(serviceId, serviceName, null, interfaceMap);
        newService.setGenerated(true);
        newService.setMaxUserCap(Math.min(service1.getMaxUserCap(), service2.getMaxUserCap()));
        newService.setResource(service1.getResource().max(service2.getResource()));

        this.addNewService(newService);
        this.addNewJob(this.getBuildJob(newService));
    }

    /**
     * Generate a new MCBuildJob according to given new generated composited service
     * @param compositedService
     * @return
     */
    private MCBuildJob getBuildJob(MService compositedService) {
        MServerOperator operator = MSystemModel.getIns().getOperator();
        MCompositionRequest compositionRequest = new MCompositionRequest();

        String requestId = "";      // generated, random is ok
        String serviceName = compositedService.getServiceName();
        String docker_owner = "";   // should be owned by system admin
        String docker_tag = "";     // version tag
        String docker_name = compositedService.getServiceName();
        String register_url = "";   // should be the register url of the cluster that wants to use this
        List<MClassFunctionPair> classFunctionPairs = operator.getCallChainList(compositedService);    // build with the info in service repo
        List<MArchitectInfo> dependencies = operator.getDependencies(compositedService);     // build with the info in service repo

        compositionRequest.setId(requestId);
        compositionRequest.setName(serviceName);
        compositionRequest.setDocker_owner(docker_owner);
        compositionRequest.setDocker_tag(docker_tag);
        compositionRequest.setDocker_name(docker_name);
        compositionRequest.setRegister_url(register_url);
        compositionRequest.setChain_list(classFunctionPairs);
        compositionRequest.setDependencies(dependencies);

        MCBuildJob mcBuildJob = new MCBuildJob();
        mcBuildJob.setCompositionRequest(compositionRequest);

        return mcBuildJob;
    }

    List<MClassFunctionPair> getCallChainList(MService compositedService) {
        List<MClassFunctionPair> resultList = new ArrayList<>();
        if (compositedService.isGenerated()) {
            if (compositedService.getAllInterface().size() != 1) {
                throw new RuntimeException("Sorry, we do not allow two or more interfaces in one composited service");
            }

            for (MServiceInterface mInterface : compositedService.getAllInterface()) {
                if (mInterface.isGenerated()) {
                    resultList.addAll(this.getCallChainListFromInterface(mInterface));
                }
            }
        }
        return resultList;
    }

    private List<MClassFunctionPair> getCallChainListFromInterface(MServiceInterface mServiceInterface) {
        List<MClassFunctionPair> resultList = new ArrayList<>();
        if (mServiceInterface.isGenerated()) {
            for (String interfaceId : mServiceInterface.getCompositionList()) {
                resultList.addAll(this.getCallChainListFromInterface(this.serviceManager.getInterfaceById(interfaceId)));
            }
        } else {
            resultList.add(mServiceInterface.toClassFuncPair());
        }
        return resultList;
    }
    // ------------------- Composition Part Ends --------------------

    public List<MArchitectInfo> getDependencies(MService compositedService) {
        List<MArchitectInfo> resultList = new ArrayList<>();
        if (compositedService.isGenerated()) {
            for (MServiceInterface mInterface : compositedService.getAllInterface()) {
                for (String interfaceId : mInterface.getCompositionList()) {
                    MServiceInterface serviceInterface = this.serviceManager.getInterfaceById(interfaceId);
                    resultList.add(this.getServiceById(serviceInterface.getServiceId()).getArtifactInfo());
                }
            }
        }
        return resultList;
    }

    MResource getNodeLeftResource(String nodeId) {
        return this.nodeId2ResourceLeft.get(nodeId);
    }

    Integer getInstanceLeftCap(String instanceId) {
        return this.insId2LeftCap.getOrDefault(instanceId, 0);
    }

    public MServiceInstance getInstanceById(String instanceId) {
        return this.instanceManager.getById(instanceId).orElse(null);
    }

    public MService getServiceById(String serviceId) {
        return this.serviceManager.getById(serviceId).orElse(null);
    }

    public List<MService> getAllServices() {
        List<MService> serviceList = this.serviceManager.getAllValues();
        serviceList.sort(new Comparator<MService>() {
            @Override
            public int compare(MService o1, MService o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return serviceList;
    }

    public List<MServiceInstance> getInstancesOfService(String serviceId) {
        return this.instanceManager.getInstancesOfService(serviceId);
    }

    public double calcCost() {
        List<MUser> userList = MSystemModel.getIns().getUserManager().getAllValues();
        double allScore = 0;
        Long chainCount = 0L;
        for (MUser user : userList) {
            String nodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(user.getPosition());
            for (MDemandChain demandChain : user.getDemandChainList()) {
                double tDelay = 0;
                double tTrans = 0;
                String prevNodeId = nodeId;
                for (MUserDemand demand : demandChain.getDemandList()) {
                    Optional<MDemandState> demandStateOptional = this.demandStateManager.getById(demand.getId());
                    if (demandStateOptional.isPresent()) {
                        MNodeConnectionInfo info = MSystemModel.getIns().getMSNManager()
                                .getConnectionInfo(prevNodeId, demandStateOptional.get().getNodeId());

                        MServiceInterface serviceInterface = this.serviceManager.getInterfaceById(demandStateOptional.get().getInterfaceId());
                        tDelay = info.getDelay();
                        tTrans = (double) (serviceInterface.getInDataSize() + serviceInterface.getOutDataSize()) / info.getBandwidth();
                    } else {
                        tDelay = MAdaptiveSystem.UNAVAILABLE_TOLERANCE;
                        tTrans = MAdaptiveSystem.UNAVAILABLE_TRANSFORM_TIME;
                    }
                    prevNodeId = demandStateOptional.get().getNodeId();
                    allScore += MAdaptiveSystem.ALPHA / (1 + tTrans) + (1 - MAdaptiveSystem.ALPHA) / (1 + tDelay);
                }
            }
            chainCount += user.getDemandChainList().size();
        }
        return allScore / chainCount;
    }
}
