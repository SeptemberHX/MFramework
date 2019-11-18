package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.utils.MIDUtils;
import com.septemberhx.server.utils.MModelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/5
 *
 * This class is used to assign given user demands in given snapshot.
 *
 * It will use HA algorithm.
 *
 * This algorithm will be used in both minor and major algorithms
 */
public class MDemandAssignHA {

    private static Logger logger = LogManager.getLogger(MDemandAssignHA.class);

    // composition version
    public static List<MBaseJob> calc_compVersion(List<MUserDemand> userDemands, MServerOperator snapshotOperator) {
        int rawJobListSize = snapshotOperator.getJobList().size();

        Map<String, MUserDemand> demandMap = new HashMap<>();
        Set<String> userIdSet = new HashSet<>();
        for (MUserDemand userDemand : userDemands) {
            demandMap.put(userDemand.getId(), userDemand);
            userIdSet.add(userDemand.getUserId());
        }

        // get node list for each user
        Map<String, List<MServerNode>> userId2NodeList = new HashMap<>();
        for (String userId : userIdSet) {
            MUser mUser = MSystemModel.getIns().getUserManager().getById(userId).get();
            String closestNodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(mUser.getPosition());
            Optional<MServerNode> closestNodeOption = MSystemModel.getIns().getMSNManager().getById(closestNodeId);
            MServerNode closestNode = closestNodeOption.get();
            List<MServerNode> serverNodeList = MSystemModel.getIns().getMSNManager().getConnectedNodesDecentWithDelayTolerance(closestNodeId);
            serverNodeList.add(0, closestNode);
            userId2NodeList.put(userId, serverNodeList);
        }

        for (MUserDemand userDemand : userDemands) {
            if (!demandMap.containsKey(userDemand.getId())) {
                continue;
            }

            // 1. find available composited-version services for this demand in his demand chains
            List<Triplet<MService, MServiceInterface, List<MUserDemand>>> potentialPairList =
                    MCompositionAlgorithmInCommon.potentialPairListMap.get(userDemand.getId());

            boolean assignSuccessfully = false;
            for (MServerNode node : userId2NodeList.get(userDemand.getUserId())) {
                // 2.1 find instance of com-service with enough capability
                for (Triplet<MService, MServiceInterface, List<MUserDemand>> potentialPair : potentialPairList) {
                    // if other demands are satisfied by other com-service, we will ignore this potential solution
                    List<MUserDemand> demandChain = potentialPair.getValue2();
                    boolean flag = true;
                    List<MDemandState> oldDemandState = new ArrayList<>();
                    for (MUserDemand demand : demandChain) {
                        Optional<MDemandState> stateOptional = snapshotOperator.getDemandStateManager().getById(demand.getId());
                        if (stateOptional.isPresent()) {
                            MDemandState state = stateOptional.get();
                            oldDemandState.add(state);
                            if (state.isAssignAsComp()) {
                                flag = false;
                            }
                            break;
                        }
                    }
                    if (!flag) {
                        continue;
                    }

                    // now, all other demands are assigned to simple service or not assigned
                    List<String> instanceIdList = snapshotOperator.getInstanceIdListOnNodeOfServiceWithEnoughCap(
                            node.getId(), potentialPair.getValue0().getId()
                    );
                    if (!instanceIdList.isEmpty()) {
                        MServiceInstance serviceInstance = snapshotOperator.getInstanceById(instanceIdList.get(0));
                        snapshotOperator.assignDemandChainIoIns(demandChain, serviceInstance, potentialPair.getValue1(), oldDemandState);
                        assignSuccessfully = true;
                    }
//                    else {
//                        String nodeId = node.getId();
//                        String serviceId = potentialPair.getValue0().getId();
//                        if (snapshotOperator.ifNodeHasResForIns(nodeId, serviceId)) {
//                            String uniqueInstanceId = MIDUtils.generateInstanceId(nodeId, serviceId);
//                            MServiceInstance newInstance = snapshotOperator.addNewInstance(serviceId, nodeId, uniqueInstanceId);
//                            snapshotOperator.assignDemandChainIoIns(demandChain, newInstance, potentialPair.getValue1(), oldDemandState);
//                            assignSuccessfully = true;
//                        }
//                    }

                    if (assignSuccessfully) {
                        // remember remove all assigned demands in demands map
                        for (MUserDemand demand : demandChain) {
                            demandMap.remove(demand.getId());
                        }
                        break;
                    }
                }
                if (assignSuccessfully) {
                    break;
                }
            }
        }

        // use raw calc to solve left demands
        MDemandAssignHA.calc(new ArrayList<>(demandMap.values()), snapshotOperator);

        return snapshotOperator.getJobList().subList(rawJobListSize, snapshotOperator.getJobList().size());
    }

    public static List<MBaseJob> calc(List<MUserDemand> userDemands, MServerOperator snapshotOperator) {
        int rawJobListSize = snapshotOperator.getJobList().size();

        Set<String> userIdSet = new HashSet<>();
        for (MUserDemand userDemand : userDemands) {
            userIdSet.add(userDemand.getUserId());
        }
        // get node list for each user
        Map<String, List<MServerNode>> userId2NodeList = new HashMap<>();
        for (String userId : userIdSet) {
            MUser mUser = MSystemModel.getIns().getUserManager().getById(userId).get();
            String closestNodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(mUser.getPosition());
            Optional<MServerNode> closestNodeOption = MSystemModel.getIns().getMSNManager().getById(closestNodeId);
            MServerNode closestNode = closestNodeOption.get();
            List<MServerNode> serverNodeList = MSystemModel.getIns().getMSNManager().getConnectedNodesDecentWithDelayTolerance(closestNodeId);
            serverNodeList.add(0, closestNode);
            userId2NodeList.put(userId, serverNodeList);
        }

        // deal with all not good demands
        for (MUserDemand userDemand : userDemands) {
            Optional<MUser> mUserOptional = MSystemModel.getIns().getUserManager().getById(userDemand.getUserId());
            if (!mUserOptional.isPresent()) {
                continue;
            }

            // Step 1: get all available nodes for this user according to max delay
            List<MServerNode> serverNodeList = userId2NodeList.get(userDemand.getUserId());

            // Step 2: try to satisfy each demand not meet
            Optional<MDemandState> demandStateOp = MSystemModel.getIns().getDemandStateManager().getById(userDemand.getId());
            MDemandState demandState = demandStateOp.orElse(null);

            // Step 2.1: try to find an exist instance
            boolean isSuccess = false;
            for (MServerNode serverNode : serverNodeList) {
                List<MServiceInstance> candidateList = snapshotOperator.getInstancesCanMetWithEnoughCapOnNode(serverNode.getId(), userDemand);
                if (candidateList.size() > 0) {
                    snapshotOperator.assignDemandToIns(userDemand, candidateList.get(0), demandState);
                    isSuccess = true;
                    break;
                }
            }

            // step 2.2: try to create a new instance for it
            if (!isSuccess) {
                MService bestService = MSystemModel.getIns().getOperator().findBestServiceToCreate(userDemand);
                if (bestService == null) {
                    logger.warn("Demand failed to find a suitable service: " + userDemand);
                    continue;
                }

                for (MServerNode serverNode : serverNodeList) {
                    if (snapshotOperator.ifNodeHasResForIns(serverNode.getId(), bestService.getId())) {
                        String uniqueInstanceId = MIDUtils.generateInstanceId(
                                serverNode.getId(),
                                bestService.getId()
                        );
                        MServiceInstance newInstance = snapshotOperator.addNewInstance(bestService.getId(), serverNode.getId(), uniqueInstanceId);
                        snapshotOperator.assignDemandToIns(userDemand, newInstance, demandState);
                        isSuccess = true;
                        break;
                    }
                }
            }

            // step 2.3: try to adjust instance resources higher
            if (!isSuccess) {
                for (MServerNode serverNode : serverNodeList) {
                    List<MServiceInstance> targetInstanceList = snapshotOperator.getInstancesCanMet(serverNode.getId(), userDemand);
                    for (MServiceInstance instance : targetInstanceList) {
                        MService oldS = snapshotOperator.getServiceById(instance.getServiceId());
                        List<MService> targetServiceList = snapshotOperator.getServiceManager().getHighRIdOfServiceOrdered(oldS);

                        for (MService tService : targetServiceList) {
                            if (snapshotOperator.checkIfCanAdjust(instance, tService)) {
                                List<MUserDemand> userDemandList = snapshotOperator.adjustInstance(instance.getId(), tService);
                                snapshotOperator.assignDemandToIns(userDemand, instance, demandState);
                                if (userDemandList.size() != 0) {
                                    throw new RuntimeException("High RId with low capability: " + tService.getId() + " and " + oldS.getId());
                                }
                                isSuccess = true;
                                break;
                            }
                        }

                        if (isSuccess) {
                            break;
                        }
                    }

                    if (isSuccess) {
                        break;
                    }
                }
            }
            if (!isSuccess) {
                throw new RuntimeException("Demand cannot be satisfied! : " + userDemand.toString());
            }
        }

        return snapshotOperator.getJobList().subList(rawJobListSize, snapshotOperator.getJobList().size());
    }
}
