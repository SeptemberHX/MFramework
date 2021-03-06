package com.septemberhx.server.core;

import com.septemberhx.common.base.*;
import com.septemberhx.common.bean.MCompositionRequest;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.adaptive.algorithm.ga.Configuration;
import com.septemberhx.common.base.MNodeConnectionInfo;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.job.*;
import com.septemberhx.server.utils.MIDUtils;
import com.septemberhx.server.utils.MModelUtils;
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
    @Getter
    private MDemandStateManager demandStateManager;
    @Getter
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

        this.demandStateManager = new MDemandStateManager();
        this.instanceManager = new MServiceInstanceManager();
        this.serviceManager = new MServiceManager();
        this.generatedInterfaceList = new ArrayList<>();
    }

    public static MServerOperator blankObject() {
        MServerOperator serverOperator = new MServerOperator();
        serverOperator.demandStateManager = new MDemandStateManager();
        serverOperator.instanceManager = new MServiceInstanceManager();
        serverOperator.serviceManager = MSystemModel.getIns().getOperator().getServiceManager().shallowClone();

        serverOperator.jobList.clear();
        serverOperator.insId2LeftCap.clear();

        serverOperator.nodeId2ResourceLeft.clear();
        // get all resources for each node
        for (MServerNode node : MSystemModel.getIns().getMSNManager().getAllValues()) {
            serverOperator.nodeId2ResourceLeft.put(node.getId(), node.getResource().deepClone());
        }

        serverOperator.generatedInterfaceList = serverOperator.serviceManager.getAllComInterfaces();
        return serverOperator;
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
            nodeId2ResourceLeftClone.put(nodeId, this.nodeId2ResourceLeft.get(nodeId).deepClone());
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
            this.nodeId2ResourceLeft.put(node.getId(), node.getResource().deepClone());
        }
        // and sub the used resources; now not used because we didn't update the MServerState
        for (MServerState serverState : this.objectMap.values()) {
            Optional<MServerNode> nodeOptional = MSystemModel.getIns().getMSNManager().getById(serverState.getId());
            nodeOptional.ifPresent(serverNode ->
                this.nodeId2ResourceLeft.put(serverState.getId(), serverNode.getResource().sub(serverState.getResource()))
            );
        }
        // we calculate the used resources by removing all the resources used by instances
        for (MServiceInstance instance : this.instanceManager.getAllValues()) {
            MService service = this.serviceManager.getById(instance.getServiceId()).get();
            if (this.nodeId2ResourceLeft.containsKey(instance.getNodeId())) {
                this.nodeId2ResourceLeft.put(
                        instance.getNodeId(),
                        this.nodeId2ResourceLeft.get(instance.getNodeId()).sub(service.getResource())
                );
            }
        }

        this.generatedInterfaceList = this.serviceManager.getAllComInterfaces();
    }

    public void printStatus() {
        Map<String, List<MDemandState>> demandStateMap = new HashMap<>();
        for (MDemandState demandState : this.demandStateManager.getAllValues()) {
            if (!demandStateMap.containsKey(demandState.getInstanceId())) {
                demandStateMap.put(demandState.getInstanceId(), new ArrayList<>());
            }
            demandStateMap.get(demandState.getInstanceId()).add(demandState);
        }

        System.out.println("=================== Instance Status ====================");
        Map<String, List<MServiceInstance>> instanceMap = new HashMap<>();
        for (MServiceInstance instance : this.instanceManager.getAllValues()) {
            if (!instanceMap.containsKey(instance.getNodeId())) {
                instanceMap.put(instance.getNodeId(), new ArrayList<>());
            }
            instanceMap.get(instance.getNodeId()).add(instance);
        }

        for (String nodeId : MSystemModel.getIns().getMSNManager().getAllValues().stream().map(MBaseObject::getId).collect(Collectors.toList())) {
            System.out.println(">>>>>>>>>>>> Node: " + nodeId);
            System.out.println(String.format("%s, left resource = %s", nodeId, this.nodeId2ResourceLeft.get(nodeId)));
            System.out.println("|");
            for (MServiceInstance instance : instanceMap.getOrDefault(nodeId, new ArrayList<>())) {
                System.out.println(String.format("|--- %s, left cap = %d, service = %s", instance.getId(), this.insId2LeftCap.get(instance.getId()), instance.getServiceId()));
//                System.out.println("   |");
//                for (MDemandState demandState : demandStateMap.getOrDefault(instance.getId(), new ArrayList<>())) {
//                    System.out.println(String.format("   |--- %s", demandState.toString()));
//                }
//                System.out.println();
            }
        }

//        System.out.println("================== Job ===================");
//        for (MBaseJob baseJob : this.jobList) {
//            System.out.println(baseJob);
//        }
    }

    public List<MUserDemand> filterNotIn(List<MUserDemand> userDemands) {
        return userDemands.stream().filter(d -> !this.demandStateManager.containsById(d.getId())).collect(Collectors.toList());
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
        String oldInstanceId = null;
        if (oldState != null) {
            this.removeDemandState(oldState);
            oldInstanceId = oldState.getInstanceId();
        }
        this.insId2LeftCap.put(instance.getId(), this.insId2LeftCap.getOrDefault(instance.getId(), 0) - 1);
//        this.jobList.add(new MSwitchJob(userDemand.getId(), instance.getId(), oldInstanceId));

        MDemandState newDemandState = this.assignDemandToInterfaceOnSpecificInstance(userDemand, instance);
        this.demandStateManager.add(newDemandState);

//        if (Configuration.DEBUG_MODE) {
//            logger.info("Assign demand " + userDemand.getId() + " to " + instance.getId());
//            if (!this.verify()) {
//                logger.error("Verify failed after assign demand to instance");
//            }
//        }

        return newDemandState;
    }

    public void assignDemandChainIoIns(List<MUserDemand> demandList, MServiceInstance instance,
                                       MServiceInterface serviceInterface, List<MDemandState> oldDemandStateList) {
        for (MDemandState oldState : oldDemandStateList) {
            if (oldState.isAssignAsComp()) {
                throw new RuntimeException("Remove old com-service demand state in assignDemand is not supported");
            }
            this.removeDemandState(oldState);
        }
        this.insId2LeftCap.put(instance.getId(), this.insId2LeftCap.getOrDefault(instance.getId(), 0) - 1);

        Optional<MService> serviceOptional = this.serviceManager.getById(instance.getServiceId());
        String comAssignId = MIDUtils.generateComDemandAssignId();
        for (MUserDemand demand : demandList) {
            if (serviceOptional.isPresent()) {
                MDemandState state = new MDemandState(demand);
                state.compSatisfy(instance, serviceInterface, comAssignId);
                this.demandStateManager.add(state);
            }
        }
    }

    public void removeDemandState(MDemandState oldState) {
        if (this.demandStateManager.containsById(oldState.getId())) {
            if (this.insId2LeftCap.containsKey(oldState.getInstanceId())) {
                this.insId2LeftCap.put(oldState.getInstanceId(), this.insId2LeftCap.get(oldState.getInstanceId()) + 1);
            }
            this.demandStateManager.deleteById(oldState.getId());
        }
    }

    public MServiceInstance addNewInstance(String serviceId, String nodeId, String instanceId) {

        if (Configuration.DEBUG_MODE) {
            if (!this.verify()) {
                logger.error("Failed to verify before addNewInstance");
            }
//            logger.info("Before addNewInstance: " + this.nodeId2ResourceLeft.get(nodeId));
        }

        Optional<MService> serviceOptional = this.serviceManager.getById(serviceId);

        MServiceInstance instance = new MServiceInstance(
                null,
                nodeId,
                null,
                null,
                instanceId,
                null,
                serviceOptional.get().getServiceName(),
                serviceId, null);
        this.instanceManager.add(instance);
        serviceOptional.ifPresent(mService -> {
            this.nodeId2ResourceLeft.get(nodeId).assign(mService.getResource());
            this.insId2LeftCap.put(instance.getId(), mService.getMaxUserCap());

            if (Configuration.DEBUG_MODE) {
//                logger.info("After addNewInstance: " + this.nodeId2ResourceLeft.get(nodeId));
//                logger.info("Target service: " + mService.getResource());
                if (!this.verify()) {
                    logger.error("Failed to verify after addNewInstance");
                    logger.debug(instance);
                    logger.debug(mService);
                }
            }
        });
        this.addNewJob(new MDeployJob(nodeId, serviceId, instanceId));
        return instance;
    }

    public List<MUserDemand> deleteInstance(String instanceId) {
        return this.deleteInstance(instanceId, true);
    }

    public List<MUserDemand> deleteInstance(String instanceId, boolean ifAddJob) {
        if (Configuration.DEBUG_MODE) {
            if (!this.verify()) {
                logger.error("Failed to verify before deleteInstance");
            }
        }

        // collect user demands on this instance
        List<MUserDemand> userDemands = new ArrayList<>();

        // allow delete non-exist instance here to improve availability
        if (!this.instanceManager.containsById(instanceId) && !this.insId2LeftCap.containsKey(instanceId)) {
            return userDemands;
        }

        for (MDemandState demandState : this.demandStateManager.getDemandStatesOnInstance(instanceId)) {
            try {
                userDemands.add(
                        MSystemModel.getIns()
                                .getUserManager()
                                .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getId()));
                this.removeDemandState(demandState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // remove instance data
        this.insId2LeftCap.remove(instanceId);
        MServiceInstance instance = this.getInstanceById(instanceId);
        Optional<MService> serviceOptional = this.serviceManager.getById(instance.getServiceId());

        if (!serviceOptional.isPresent()) {
            logger.error(instance);
        }

        serviceOptional.ifPresent(mService -> {
            String nodeId = this.instanceManager.getById(instanceId).get().getNodeId();
            if (Configuration.DEBUG_MODE) {
                logger.info("Before resource release " + this.nodeId2ResourceLeft.get(nodeId));
            }
            this.nodeId2ResourceLeft.get(nodeId).free(mService.getResource());
            if (Configuration.DEBUG_MODE) {
                logger.info("After resource release " + this.nodeId2ResourceLeft.get(nodeId));
            }
        });
        this.instanceManager.delete(instanceId);
        if (ifAddJob) {
            this.addNewJob(new MDeleteJob(instanceId, instance.getServiceId(), instance.getNodeId()));
        }

        if (Configuration.DEBUG_MODE) {
            if (!this.verify()) {
                logger.error("Failed to verify after deleteInstance");
                logger.debug(instance);
            }
        }

        return userDemands;
    }

    public boolean moveInstance(String instanceId, String targetNodeId) {
        Optional<MServiceInstance> instanceOptional = this.instanceManager.getById(instanceId);
        if (!instanceOptional.isPresent()) {
            return false;
        }
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
        for (MDemandState demandState : this.demandStateManager.getDemandStatesOnInstance(instanceId)) {
            MDemandState newState = new MDemandState(
                    demandState.getId(),
                    demandState.getInstanceId(),
                    demandState.getInterfaceId(),
                    demandState.getUserId(),
                    targetNodeId,
                    demandState.getComAssignId()
            );
            this.demandStateManager.replace(newState);
        }

        this.addNewJob(new MMoveJob(instanceId, targetNodeId, instance.getNodeId(), instance.getServiceId()));
        return true;
    }

    public List<MServiceInstance> getInstancesOnNode(String nodeId) {
        return new ArrayList<>(this.instanceManager.getInstancesOnNode(nodeId));
    }

    public List<String> getInstanceIdListOnNodeOfService(String nodeId, String serviceId) {
        List<String> resultList = new LinkedList<>();
        for (MServiceInstance serviceInstance : this.instanceManager.getInstancesOnNode(nodeId)) {
            if (serviceInstance.getServiceId().equals(serviceId)) {
                resultList.add(serviceInstance.getId());
            }
        }
        return resultList;
    }

    public List<String> getInstanceIdListOnNodeOfServiceWithEnoughCap(String nodeId, String serviceId) {
        List<String> resultList = new LinkedList<>();
        for (MServiceInstance serviceInstance : this.instanceManager.getInstancesOnNode(nodeId)) {
            if (serviceInstance.getServiceId().equals(serviceId) && this.insId2LeftCap.get(serviceInstance.getId()) > 0) {
                resultList.add(serviceInstance.getId());
            }
        }
        return resultList;
    }

    public List<MServiceInstance> getInstancesCanMetWithEnoughCapOnNode(String nodeId, MUserDemand userDemand) {
        List<MServiceInstance> instanceList = this.getInstancesOnNode(nodeId);
        List<MServiceInstance> resultList = new ArrayList<>();
        for (MServiceInstance instance : instanceList) {
            if (!this.ifInstanceHasCap(instance.getId(), 1)) {
                continue;
            }

            if (userDemand.getServiceId() != null && !instance.getServiceId().startsWith(userDemand.getServiceId())) {
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

    public List<MServiceInstance> getInstancesCanMet(String nodeId, MUserDemand userDemand) {
        List<MServiceInstance> instanceList = this.getInstancesOnNode(nodeId);
        List<MServiceInstance> resultList = new ArrayList<>();
        for (MServiceInstance instance : instanceList) {

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
        MService newService = MModelUtils.compService(service1, interface1, service2, interface2);
        this.addNewService(newService);
        MCBuildJob mcBuildJob = this.getBuildJob(newService);
        newService.setDockerImageUrl(mcBuildJob.getImageFullName());
        this.addNewJob(mcBuildJob);
    }

    /**
     * Generate a new MCBuildJob according to given new generated composited service
     * @param compositedService
     * @return
     */
    private MCBuildJob getBuildJob(MService compositedService) {
        MCompositionRequest compositionRequest = new MCompositionRequest();

        String requestId = "";      // generated, random is ok
        String serviceName = compositedService.getServiceName();
        String docker_owner = MBuildJob.OWNER;   // should be owned by system admin
        String docker_tag = "v1.0";     // version tag
        String docker_name = compositedService.getServiceName();
        String register_url = "";   // should be the register url of the cluster that wants to use this
        List<MClassFunctionPair> classFunctionPairs = this.getCallChainList(compositedService);    // build with the info in service repo
        List<MArchitectInfo> dependencies = this.getDependencies(compositedService);     // build with the info in service repo

        compositionRequest.setName(serviceName);
        compositionRequest.setDocker_owner(docker_owner);
        compositionRequest.setDocker_tag(docker_tag);
        compositionRequest.setDocker_name(docker_name);
        compositionRequest.setRegister_url(register_url);
        compositionRequest.setChain_list(classFunctionPairs);
        compositionRequest.setDependencies(dependencies);

        MCBuildJob mcBuildJob = new MCBuildJob();
        compositionRequest.setId(mcBuildJob.getId());
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

    public double calcScore() {
        List<MUser> userList = MSystemModel.getIns().getUserManager().getAllValues();
        double allScore = 0;
        Long chainCount = 0L;
        for (MUser user : userList) {
            String nodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(user.getPosition());
            for (MDemandChain demandChain : user.getDemandChainList()) {
                double tDelay = 0;
                double tTrans = 0;
                for (MUserDemand demand : demandChain.getDemandList()) {
                    Optional<MDemandState> demandStateOptional = this.demandStateManager.getById(demand.getId());
                    if (demandStateOptional.isPresent()) {
                        MNodeConnectionInfo info = MSystemModel.getIns().getMSNManager()
                                .getConnectionInfo(nodeId, demandStateOptional.get().getNodeId());

                        MServiceInterface serviceInterface = this.serviceManager.getInterfaceById(demandStateOptional.get().getInterfaceId());
                        tDelay = info.getDelay();
                        tTrans = (double) (serviceInterface.getInDataSize() + serviceInterface.getOutDataSize()) / info.getBandwidth();
                    } else {
                        tDelay = MAdaptiveSystem.UNAVAILABLE_TOLERANCE;
                        tTrans = MAdaptiveSystem.UNAVAILABLE_TRANSFORM_TIME;
                    }
                    allScore += MAdaptiveSystem.ALPHA / (1 + tTrans) + (1 - MAdaptiveSystem.ALPHA) / (1 + tDelay);
                }
            }
            chainCount += user.getDemandChainList().size();
        }
        return allScore / chainCount;
    }

    public double calcScore_v2() {
        List<MUser> userList = MSystemModel.getIns().getUserManager().getAllValues();
        double allScore = 0;
        Long chainCount = 0L;
        for (MUser user : userList) {
            String nodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(user.getPosition());
            for (MDemandChain demandChain : user.getDemandChainList()) {
                double tDelay = 0;
                double tTrans = 0;
                String prevDemandStateAssignId = null;
                for (MUserDemand demand : demandChain.getDemandList()) {
                    Optional<MDemandState> demandStateOptional = this.demandStateManager.getById(demand.getId());
                    if (demandStateOptional.isPresent()) {
                        if (prevDemandStateAssignId != null
                                && demandStateOptional.get().isAssignAsComp()
                                && demandStateOptional.get().getComAssignId().equals(prevDemandStateAssignId)) {
                            continue;
                        }

                        MServiceInterface serviceInterface = this.serviceManager.getInterfaceById(demandStateOptional.get().getInterfaceId());
                        if (!demandStateOptional.get().getNodeId().equals(nodeId)) {
                            MNodeConnectionInfo info = MSystemModel.getIns().getMSNManager()
                                    .getConnectionInfo(nodeId, demandStateOptional.get().getNodeId());
                            tDelay = info.getDelay() * 2;
                            tTrans = (double) (serviceInterface.getInDataSize() + serviceInterface.getOutDataSize()) / info.getBandwidth();
                        }

                        MServerNode node = MSystemModel.getIns().getMSNManager().getById(nodeId).get();
                        tDelay += node.getDelay() * 2;  // delay between user and node
                        tTrans += (double) (serviceInterface.getInDataSize() + serviceInterface.getOutDataSize()) / node.getBandwidth();

                        prevDemandStateAssignId = demandStateOptional.get().getComAssignId();
                    } else {
                        tDelay = MAdaptiveSystem.UNAVAILABLE_TOLERANCE;
                        tTrans = MAdaptiveSystem.UNAVAILABLE_TRANSFORM_TIME;
                    }
                    allScore += MAdaptiveSystem.ALPHA * tTrans + (1 - MAdaptiveSystem.ALPHA) * tDelay;
                }
            }
            chainCount += user.getDemandChainList().size();
        }
        return allScore / chainCount;
    }

    public double calcEvolutionCost() {
        return this.jobList.stream().mapToDouble(MBaseJob::cost).sum();
    }

    public double calcEvolutionCost_v2(MServerOperator rawOperator) {
        List<MServiceInstance> allInstance = rawOperator.getAllInstances();

        // classify all instances by node id and service name
        Map<String, Map<String, List<MServiceInstance>>> nodeId2ServiceName2Instance = new HashMap<>();
        for (MServiceInstance instance : allInstance) {
            if (!nodeId2ServiceName2Instance.containsKey(instance.getNodeId())) {
                nodeId2ServiceName2Instance.put(instance.getNodeId(), new HashMap<>());
            }

            if (!nodeId2ServiceName2Instance.get(instance.getNodeId()).containsKey(instance.getServiceName())) {
                nodeId2ServiceName2Instance.get(instance.getNodeId()).put(instance.getServiceName(), new ArrayList<>());
            }
            nodeId2ServiceName2Instance.get(instance.getNodeId()).get(instance.getServiceName()).add(instance);
        }

        // do the same with current instance list
        List<MServiceInstance> allInstanceNow = this.getAllInstances();
        Map<String, Map<String, List<MServiceInstance>>> nId2SName2InstNow = new HashMap<>();
        for (MServiceInstance instance : allInstanceNow) {
            if (!nId2SName2InstNow.containsKey(instance.getNodeId())) {
                nId2SName2InstNow.put(instance.getNodeId(), new HashMap<>());
            }

            if (!nId2SName2InstNow.get(instance.getNodeId()).containsKey(instance.getServiceName())) {
                nId2SName2InstNow.get(instance.getNodeId()).put(instance.getServiceName(), new ArrayList<>());
            }
            nId2SName2InstNow.get(instance.getNodeId()).get(instance.getServiceName()).add(instance);
        }


        // move operation is remove & add & switch
        int deploy_num = 0;
        int remove_num = 0;
        int adjust_num = 0;
        int switch_num = 0;

        // composition is compile new service & deploy
        // split is remove & deploy
        int composition_num = 0;

        for (MServerNode node : MSystemModel.getIns().getMSNManager().getAllValues()) {
            String nodeId = node.getId();

            Set<String> serviceNameSet = new HashSet<>();
            if (nId2SName2InstNow.containsKey(nodeId)) {
                serviceNameSet.addAll(nId2SName2InstNow.get(nodeId).keySet());
            }

            if (nodeId2ServiceName2Instance.containsKey(nodeId)) {
                serviceNameSet.addAll(nodeId2ServiceName2Instance.get(nodeId).keySet());
            }

            for (String serviceName : serviceNameSet) {
                int negative_sum = 0;
                int passive_sum = 0;

                Map<String, Integer> serviceId2NumNow = new HashMap<>();
                for (MServiceInstance serviceInstance : nId2SName2InstNow.getOrDefault(nodeId, new HashMap<>()).getOrDefault(serviceName, new ArrayList<>())) {
                    if (!serviceId2NumNow.containsKey(serviceInstance.getServiceId())) {
                        serviceId2NumNow.put(serviceInstance.getServiceId(), 0);
                    }
                    serviceId2NumNow.put(serviceInstance.getServiceId(), 1 + serviceId2NumNow.get(serviceInstance.getServiceId()));
                }

                Map<String, Integer> serviceId2NumOld = new HashMap<>();
                for (MServiceInstance serviceInstance : nodeId2ServiceName2Instance.getOrDefault(nodeId, new HashMap<>()).getOrDefault(serviceName, new ArrayList<>())) {
                    if (!serviceId2NumOld.containsKey(serviceInstance.getServiceId())) {
                        serviceId2NumOld.put(serviceInstance.getServiceId(), 0);
                    }
                    serviceId2NumOld.put(serviceInstance.getServiceId(), 1 + serviceId2NumOld.get(serviceInstance.getServiceId()));
                }

                Set<String> serviceIdSet = new HashSet<>();
                serviceIdSet.addAll(serviceId2NumNow.keySet());
                serviceIdSet.addAll(serviceId2NumOld.keySet());
                for (String serviceId : serviceIdSet) {
                    int t = serviceId2NumNow.getOrDefault(serviceId, 0) - serviceId2NumOld.getOrDefault(serviceId, 0);
                    if (t >= 0) {
                        passive_sum += t;
                    } else {
                        negative_sum += t;
                    }
                }

                int sumT = passive_sum + negative_sum;
                if (sumT >= 0) {
                    deploy_num += sumT;
                    adjust_num += -negative_sum;
                } else {
                    remove_num += -sumT;
                    adjust_num += passive_sum;
                }
            }
        }

        for (MDemandState demandState : this.demandStateManager.getAllValues()) {
            Optional<MDemandState> demandStateOptional = rawOperator.getDemandStateManager().getById(demandState.getId());
            if (demandStateOptional.isPresent()) {
                if (!demandState.getNodeId().equals(demandStateOptional.get().getNodeId())) {
                    switch_num += 1;
                }
            } else {
                switch_num += 1;
            }
        }

        Set<String> serviceUsedSet = new HashSet<>();
        for (MServiceInstance instance : this.getAllInstances()) {
            serviceUsedSet.add(instance.getServiceId());
        }
        for (MService service : this.serviceManager.getAllValues()) {
            if (!rawOperator.getServiceManager().containsById(service.getId()) && service.isGenerated() && !serviceUsedSet.contains(service.getId())) {
                composition_num += 1;
            }
        }

        return deploy_num * MBaseJob.COST_DEPLOY + remove_num * MBaseJob.COST_REMOVE + adjust_num * MBaseJob.COST_ADJUST
                + switch_num * MBaseJob.COST_SWITCH + composition_num * MBaseJob.COST_COMPOSITION;
    }

    public double calcEvolutionCost(MServerOperator rawOperator) {
        List<MBaseJob> newJobList = new ArrayList<>();

        // Service part
        List<MService> serviceListNow = this.serviceManager.getAllValues();
        for (MService service : serviceListNow) {
            if (!rawOperator.serviceManager.containsById(service.getId())) {
                newJobList.add(this.getBuildJob(service));
            }
        }

        // Instance part
        // use exist instances if possible
        for (MServerNode node : MSystemModel.getIns().getMSNManager().getAllValues()) {
            List<MServiceInstance> instanceListNow = this.getInstancesOnNode(node.getId());
            List<MServiceInstance> instanceListOld = rawOperator.getInstancesOnNode(node.getId());

            Set<String> currInstanceIdSet = new HashSet<>();
            instanceListNow.forEach(i -> currInstanceIdSet.add(i.getId()));
            Set<String> oldInstanceIdSet = new HashSet<>();
            instanceListOld.forEach(i -> oldInstanceIdSet.add(i.getId()));

            for (String currId : currInstanceIdSet) {
                if (!oldInstanceIdSet.contains(currId)) {
                    newJobList.add(new MDeployJob(node.getId(), this.getInstanceById(currId).getServiceId(), currId));
                }
            }
            for (String oldId : oldInstanceIdSet) {
                if (!currInstanceIdSet.contains(oldId)) {
                    newJobList.add(new MDeleteJob(oldId, null, null));
                }
            }
        }

        // User part
        for (MDemandState demandState : this.demandStateManager.getAllValues()) {
            Optional<MDemandState> demandStateOptional = rawOperator.demandStateManager.getById(demandState.getId());
            boolean ifNeedSwitch = false;
            String oldInstanceId = null;
            if (demandStateOptional.isPresent()) {
                MDemandState oldState = demandStateOptional.get();
                oldInstanceId = oldState.getInstanceId();
                if (!oldInstanceId.equals(oldState.getInstanceId())) {
                    ifNeedSwitch = true;
                }
            } else {
                ifNeedSwitch = true;
            }

            if (ifNeedSwitch) {
                newJobList.add(new MSwitchJob(demandState.getId(), demandState.getInstanceId(), oldInstanceId));
            }
        }

        // record the job list
        this.jobList = newJobList;
        return this.jobList.stream().mapToDouble(MBaseJob::cost).sum();
    }

    public Map<String, MDemandState> getDemandStateByInstanceIds(Set<String> instanceIdList) {
        Map<String, MDemandState> r = new HashMap<>();
        for (String instanceId : instanceIdList) {
            for (MDemandState demandState : this.demandStateManager.getDemandStatesOnInstance(instanceId)) {
                r.put(demandState.getId(), demandState);
            }
        }
        return r;
    }

    /**
     * Adjust the job list, and remove useless jobs.
     * For example: For an ADD job and an REMOVE job of the same instance, we need to delete the ADD job
     */
    public void adjustJobList() {
        Set<String> removeJobIdList = new HashSet<>();
        Map<String, List<MBaseJob>> objectId2JobList = new HashMap<>();
        Iterator<MBaseJob> jobIterator = this.jobList.iterator();
        while (jobIterator.hasNext()) {
            MBaseJob baseJob = jobIterator.next();
            String objectId = null;
            switch (baseJob.getType()) {
                case DEPLOY:
                    MDeployJob deployJob = (MDeployJob) baseJob;
                    objectId = deployJob.getUniqueId();
                    break;
                case DELETE:
                    MDeleteJob deleteJob = (MDeleteJob) baseJob;
                    objectId = deleteJob.getInstanceId();
                    break;
                case MOVE:
                    MMoveJob moveJob = (MMoveJob) baseJob;
                    objectId = moveJob.getInstanceId();
                    break;
                case SWITCH:
                    MSwitchJob switchJob = (MSwitchJob) baseJob;
                    objectId = switchJob.getUserDemandId();
                    break;
                case ADJUST:
                    MAdjustJob adjustJob = (MAdjustJob) baseJob;
                    objectId = adjustJob.getInstanceId();
                    break;
                default:
                    break;
            }

            if (objectId != null) {
                if (!objectId2JobList.containsKey(objectId)) {
                    objectId2JobList.put(objectId, new ArrayList<>());
                }
                objectId2JobList.get(objectId).add(baseJob);
            }
        }

        for (String objectId : objectId2JobList.keySet()) {
            if (objectId2JobList.get(objectId).size() <= 1) {
                continue;
            }

            List<MBaseJob> currJobList = objectId2JobList.get(objectId);
            MBaseJob lastJob = currJobList.get(currJobList.size() - 1);
            switch (lastJob.getType()) {
                case DEPLOY:
                    for (int i = 0; i < currJobList.size() - 1; ++i) {
                        removeJobIdList.add(currJobList.get(i).getId());
                    }
                    if (!this.instanceManager.containsById(((MDeployJob) lastJob).getUniqueId())) {
                        removeJobIdList.add(lastJob.getId());
                    }
                    break;
                case SWITCH:
                    for (int i = 0; i < currJobList.size() - 1; ++i) {
                        removeJobIdList.add(currJobList.get(i).getId());
                    }
                    break;
                case DELETE:
                    currJobList.forEach(j -> removeJobIdList.add(j.getId()));
                    break;
                case MOVE:
                    MDeployJob tmpDeployJob = null;
                    for (int j = 0; j < currJobList.size(); ++j) {
                        if (currJobList.get(j).getType() == MJobType.DEPLOY) {
                            tmpDeployJob = (MDeployJob) currJobList.get(j);
                            break;
                        }
                    }
                    if (tmpDeployJob != null) {
                        currJobList.forEach(j -> removeJobIdList.add(j.getId()));
                        tmpDeployJob.setNodeId(((MMoveJob) lastJob).getTargetNodeId());
                        tmpDeployJob.newId();
                        this.jobList.add(tmpDeployJob);
                    }
                    break;
                case ADJUST:
                    int j;
                    MDeployJob tDeployJob = null;
                    for (j = currJobList.size() - 1; j >= 0; --j) {
                        if (currJobList.get(j).getType() == MJobType.DEPLOY) {
                            tDeployJob = (MDeployJob) currJobList.get(j);
                            break;
                        }
                    }
                    if (tDeployJob != null) {
                        currJobList.forEach(job -> removeJobIdList.add(job.getId()));
                        tDeployJob.setServiceName(((MAdjustJob) lastJob).getTargetServiceId());
                        tDeployJob.newId();
                        this.jobList.add(tDeployJob);
                    }
                    break;
                default:
                    break;
            }
        }

        this.jobList.removeIf(currJob -> removeJobIdList.contains(currJob.getId()));
    }

    public List<MServiceInstance> getAllInstances() {
        return this.instanceManager.getAllValues();
    }

    public int getInstanceUserNumber(String instanceId) {
        Optional<MService> serviceOptional = this.serviceManager.getById(this.getInstanceById(instanceId).getServiceId());
        return serviceOptional.get().getMaxUserCap() - this.insId2LeftCap.get(instanceId);
    }

    public boolean verify() {
        Map<String, MResource> rLeft = new HashMap<>();

        // get all resources for each node
        for (MServerNode node : MSystemModel.getIns().getMSNManager().getAllValues()) {
            rLeft.put(node.getId(), node.getResource().deepClone());
        }
        // and sub the used resources
        for (MServerState serverState : this.objectMap.values()) {
            Optional<MServerNode> nodeOptional = MSystemModel.getIns().getMSNManager().getById(serverState.getId());
            nodeOptional.ifPresent(serverNode ->
                    rLeft.put(serverState.getId(), serverNode.getResource().sub(serverState.getResource()))
            );
        }
        // and sub all the resources consumed by instances
        for (MServiceInstance instance : this.getAllInstances()) {
            Optional<MService> serviceOptional = this.serviceManager.getById(instance.getServiceId());
            if (!serviceOptional.isPresent()) {
                logger.debug("No such service: " + instance.getServiceId());
                return false;
            }

            serviceOptional.ifPresent(s -> rLeft.get(instance.getNodeId()).assign(s.getResource()));
        }

        if (rLeft.size() != this.nodeId2ResourceLeft.size()) {
            logger.debug("Size not the same");
            return false;
        }

        for (String nodeId : rLeft.keySet()) {
            if (!this.nodeId2ResourceLeft.containsKey(nodeId)) {
                logger.debug("No such nodeId key: " + nodeId);
                return false;
            }

            if (!this.nodeId2ResourceLeft.get(nodeId).equals(rLeft.get(nodeId))) {
                this.printStatus();
                logger.warn("nodeId: " + nodeId + " has inconsistent resource");
                logger.warn("In leftR: " + this.nodeId2ResourceLeft.get(nodeId));
                logger.warn("It should be " + rLeft.get(nodeId));
                return false;
            }
        }

        for (MDemandState demandState : this.demandStateManager.getAllValues()) {
            if (!this.instanceManager.containsById(demandState.getInstanceId())) {
                logger.warn("Demand state assigned to non-exist instance: " + demandState.getInstanceId());
                return false;
            }
        }

        for (MServiceInstance instance : this.instanceManager.getAllValues()) {
            int userDemandsSize = 0;
            Set<String> comDemandAssignIdSet = new HashSet<>();
            for (MDemandState demandState : this.demandStateManager.getDemandStatesOnInstance(instance.getId())) {
                if (!demandState.isAssignAsComp()) {
                    userDemandsSize += 1;
                } else {
                    comDemandAssignIdSet.add(demandState.getComAssignId());
                }
            }
            userDemandsSize += comDemandAssignIdSet.size();
            int serviceCap = this.getServiceById(instance.getServiceId()).getMaxUserCap();

            if (this.insId2LeftCap.get(instance.getId()) != serviceCap - userDemandsSize) {
                this.printStatus();
                logger.warn("Instance " + instance.getId() + " has inconsistent capability");
                return false;
            }

            if (this.insId2LeftCap.get(instance.getId()) == serviceCap) {
                logger.warn("Instance with 0 users");
                return false;
            }
        }

        for (List<String> demandIdList : this.demandStateManager.getInstanceId2StateIdList().values()) {
            for (String demandStateId : demandIdList) {
                if (this.demandStateManager.getObjectMap().get(demandStateId) == null) {
                    logger.warn("DemandState " + demandStateId + " is null");
                    return false;
                }
            }
        }

        return true;
    }

    public int getSwitchJobSize() {
        int i = 0;
        for (MBaseJob job : this.jobList) {
            if (job.getType() == MJobType.SWITCH) {
                ++i;
            }
        }
        return i;
    }

    public boolean checkIfCanAdjust(MServiceInstance randomInstance, MService targetS) {
        MResource leftR = this.nodeId2ResourceLeft.get(randomInstance.getNodeId());
        Optional<MService> serviceOptional = this.serviceManager.getById(randomInstance.getServiceId());
        MResource oldR = serviceOptional.get().getResource();
        MResource newR = targetS.getResource();
        return leftR.isEnough(newR.sub(oldR));
    }

    public List<MUserDemand> adjustInstance(String instanceId, MService targetS) {

        if (Configuration.DEBUG_MODE) {
            logger.info("DemandManager size: " + this.demandStateManager.getAllValues().size() + " before adjustInstance");
            logger.info("Raw instance left capability before adjustInstance: " + this.insId2LeftCap.get(instanceId));
        }

        List<MUserDemand> unSolvedDemands = new ArrayList<>();

        // because of the reference existing in other solutions, we make a copy
        MServiceInstance randomInstance = this.getInstanceById(instanceId).deepClone();
        MResource leftR = this.nodeId2ResourceLeft.get(randomInstance.getNodeId());

        Optional<MService> serviceOptional = this.serviceManager.getById(randomInstance.getServiceId());
        MResource oldR = serviceOptional.get().getResource();
        MResource newR = targetS.getResource();

        int freeDemandSize = serviceOptional.get().getMaxUserCap() - targetS.getMaxUserCap();
        this.insId2LeftCap.put(instanceId, this.insId2LeftCap.get(instanceId) - freeDemandSize);
        if (freeDemandSize < 0) {   // high resource with high capability
            randomInstance.setServiceId(targetS.getId());
        } else {    // low resource with low capability
            // remove sla not meet user demands
            randomInstance.setServiceId(targetS.getId());
            List<MDemandState> demandStateList = this.demandStateManager.getDemandStatesOnInstance(instanceId);
            Iterator<MDemandState> demandStateIterator = demandStateList.iterator();
            while (demandStateIterator.hasNext()){
                MDemandState demandState = demandStateIterator.next();
                MUserDemand userDemand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId(
                        demandState.getUserId(),
                        demandState.getId()
                );
                if (!targetS.checkIfMeetDemand(userDemand)) {
                    this.removeDemandState(demandState);
                    demandStateIterator.remove();
                    unSolvedDemands.add(userDemand);
                }
            }

            Collections.shuffle(demandStateList, random);
            for (int i = 0; i < freeDemandSize && i < demandStateList.size(); ++i) {
                this.removeDemandState(demandStateList.get(i));
                MUserDemand userDemand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId(
                        demandStateList.get(i).getUserId(),
                        demandStateList.get(i).getId()
                );
                unSolvedDemands.add(userDemand);
            }
        }

        // once adjust an instance, the instance will not be itself anymore. It is a new instance.
        this.instanceManager.delete(instanceId);
        if (Configuration.DEBUG_MODE) {
            logger.info("Demand state size after delete raw instance: " + this.demandStateManager.getAllValues().size());
        }

        randomInstance.setId(MIDUtils.generateInstanceId(randomInstance.getNodeId(), randomInstance.getServiceId()));
        this.instanceManager.add(randomInstance);

        if (Configuration.DEBUG_MODE) {
            logger.info("Raw instance left capability after adjust: " + this.insId2LeftCap.get(instanceId));
        }

        this.insId2LeftCap.put(randomInstance.getId(), this.insId2LeftCap.get(instanceId));
        this.insId2LeftCap.remove(instanceId);

        if (Configuration.DEBUG_MODE) {
            logger.info("New adjust instance left capability after adjust: " + this.insId2LeftCap.get(randomInstance.getId()) + ", id: " + randomInstance.getId());
            logger.info("Size of demands on this instance: " + this.demandStateManager.getDemandStatesOnInstance(instanceId).size());
        }

        for (MDemandState demandState : this.demandStateManager.getDemandStatesOnInstance(instanceId)) {
            MDemandState newState = new MDemandState(
                    demandState.getId(),
                    randomInstance.getId(),
                    demandState.getInterfaceId(),
                    demandState.getUserId(),
                    demandState.getNodeId(),
                    demandState.getComAssignId()
            );
            this.demandStateManager.replace(newState);
        }

        if (Configuration.DEBUG_MODE) {
            logger.info("Size of demands on this new adjust instance: " + this.demandStateManager.getDemandStatesOnInstance(randomInstance.getId()).size());
        }

        this.addNewJob(new MAdjustJob(randomInstance.getId(), targetS.getSlaId(), serviceOptional.get().getId(), targetS.getId(), randomInstance.getNodeId()));
        leftR.free(oldR);
        leftR.assign(newR);

        if (Configuration.DEBUG_MODE) {
            logger.info("DemandManager size: " + this.demandStateManager.getAllValues().size() + " after adjustInstance");
            logger.info("Unsolved demands size: " + unSolvedDemands.size());
        }
        return unSolvedDemands;
    }

    public MServiceInterface getServiceInterface(String interfaceId) {
        return this.serviceManager.getInterfaceById(interfaceId);
    }

    /*
     * Compare current operator with the previous operator
     * and get the job lists which can evolve from previous one to current one.
     */
    public List<MBaseJob> calcJobList(MServerOperator rawOperator) {
        List<MBaseJob> baseJobList = new ArrayList<>();

        // build composition service
        for (MService service : this.serviceManager.getAllComServices()) {
            if (!rawOperator.getServiceManager().containsById(service.getId())) {
                baseJobList.add(this.getBuildJob(service));
            }
        }

        // deploy new instance
        Map<String, List<MServiceInstance>> currInstanceMap = new HashMap<>();
        for (MServiceInstance serviceInstance : this.getAllInstances()) {
            if (!currInstanceMap.containsKey(serviceInstance.getServiceId())) {
                currInstanceMap.put(serviceInstance.getServiceId(), new ArrayList<>());
            }
            currInstanceMap.get(serviceInstance.getServiceId()).add(serviceInstance);
        }

        Map<String, List<MServiceInstance>> oldInstanceMap = new HashMap<>();
        for (MServiceInstance oldInstance : rawOperator.getAllInstances()) {
            if (!oldInstanceMap.containsKey(oldInstance.getServiceId())) {
                oldInstanceMap.put(oldInstance.getServiceId(), new ArrayList<>());
            }
            oldInstanceMap.get(oldInstance.getServiceId()).add(oldInstance);
        }

        for (String currServiceId : currInstanceMap.keySet()) {
            if (!oldInstanceMap.containsKey(currServiceId)) {
                for (MServiceInstance instance : currInstanceMap.get(currServiceId)) {
                    baseJobList.add(new MDeployJob(
                            instance.getNodeId(), instance.getServiceName(), instance.getId()
                    ));
                }
            } else {
                Set<String> oldInstanceIdSet = oldInstanceMap.get(currServiceId).stream().map(MServiceInstance::getId).collect(Collectors.toSet());
                Set<String> currInstanceIdSet = currInstanceMap.get(currServiceId).stream().map(MServiceInstance::getId).collect(Collectors.toSet());

                Set<String> retainSet = new HashSet<>(currInstanceIdSet);
                retainSet.retainAll(oldInstanceIdSet);  // do nothing about this instance

                // deal with others
                // We will reuse old instance if possible
                oldInstanceIdSet.removeAll(retainSet);
                currInstanceIdSet.removeAll(retainSet);

                List<String> oldInstanceIdList = new ArrayList<>(oldInstanceIdSet);
                List<String> currInstanceIdList = new ArrayList<>(currInstanceIdSet);

                int currIndex = 0;
                for (; currIndex < oldInstanceIdList.size() && currIndex < currInstanceIdList.size(); ++currIndex) {
                    this.renameInstance(currInstanceIdList.get(currIndex), oldInstanceIdList.get(currIndex));
                }

                if (currIndex < oldInstanceIdList.size()) {
                    for (int j = currIndex; j < oldInstanceIdList.size(); ++j) {
                        String oldInstanceId = oldInstanceIdList.get(j);
                        MServiceInstance instance = rawOperator.getInstanceById(oldInstanceId);
                        baseJobList.add(
                                new MDeleteJob(oldInstanceId, instance.getServiceId(), instance.getNodeId())
                        );
                    }
                }

                if (currIndex < currInstanceIdList.size()) {
                    for (int j = currIndex; j < currInstanceIdList.size(); ++j) {
                        String currInstanceId = currInstanceIdList.get(j);
                        MServiceInstance instance = this.getInstanceById(currInstanceId);
                        baseJobList.add(
                                new MDeployJob(instance.getNodeId(), instance.getServiceName(), instance.getId())
                        );
                    }
                }
            }
        }

        // remove the instances of services that not in the calculated result
        for (String oldServiceId : oldInstanceMap.keySet()) {
            if (currInstanceMap.containsKey(oldServiceId)) {
                continue;
            }

            for (MServiceInstance instance : oldInstanceMap.get(oldServiceId)) {
                baseJobList.add(
                        new MDeleteJob(instance.getId(), instance.getServiceId(), instance.getNodeId())
                );
            }
        }

        for (MDemandState currDemandState : this.demandStateManager.getAllValues()) {
            if (rawOperator.getDemandStateManager().containsById(currDemandState.getId())) {
                MDemandState oldState = rawOperator.getDemandStateManager().getById(currDemandState.getId()).get();
                if (oldState.getUserId().equals(currDemandState.getUserId()) && oldState.getInstanceId().equals(currDemandState.getUserId())) {
                    continue;
                }
            }

            baseJobList.add(
                    new MSwitchJob(currDemandState.getId(), currDemandState.getInstanceId(), null)
            );
        }
        return baseJobList;
    }

    public void renameInstance(String oldId, String newId) {
        if (this.insId2LeftCap.containsKey(oldId)) {
            this.insId2LeftCap.put(newId, this.insId2LeftCap.get(oldId));
            this.insId2LeftCap.remove(oldId);
        }
        this.demandStateManager.renameInstance(oldId, newId);
        this.instanceManager.renameInstance(oldId, newId);
    }

    public void deleteEmptyInstance() {
        for (MServiceInstance instance : this.getAllInstances()) {
            if (this.insId2LeftCap.containsKey(instance.getId())) {
                MService service = this.serviceManager.getById(instance.getServiceId()).get();
                if (service.getMaxUserCap() == this.insId2LeftCap.get(instance.getId())) {
                    this.deleteInstance(instance.getId());
                }
            } else {
                this.deleteInstance(instance.getId());
            }
        }
    }
}
