package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MIDUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/22
 */
public class MMinorAlgorithm implements MAlgorithmInterface {

    private static Logger logger = LogManager.getLogger(MMinorAlgorithm.class);

    @Override
    public MPlannerResult calc(MAnalyserResult data) {
        MServerOperator serverOperator = MSystemModel.getIns().getOperator();
        serverOperator.reInit();

        List<EndpointPair<MSIInterface>> pCompositionList = data.getPotentialCompositionList();
        Map<String, List<MDemandState>> notMetMap = data.getAffectedUserId2MDemandStateBySla();
        Set<String> longResTimeUserIdSet = data.getAffectedUserIdByAvgTime();

        // replace composition services, and collect all demands of the longResTime users together
        List<MUserDemand> demandList = new ArrayList<>();
        for (String userId : longResTimeUserIdSet) {
            Optional<MUser> mUserOptional = MSystemModel.getIns().getUserManager().getById(userId);
            if (mUserOptional.isPresent()) {
                for (MDemandChain chain : mUserOptional.get().getDemandChainList()) {
                    demandList.addAll(this.replaceCompositionPart(chain.getDemandList()));
                }
            }
        }

        // collect all notMet user demands, too
        for (String userId : notMetMap.keySet()) {
            for (MDemandState demandState : notMetMap.get(userId)) {
                MUserDemand userDemand = MSystemModel.getIns().getUserManager()
                        .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getInstanceId());
                demandList.add(userDemand);
            }
        }

        // deal with all not good demands
        for (MUserDemand userDemand : demandList) {
            Optional<MUser> mUserOptional = MSystemModel.getIns().getUserManager().getById(userDemand.getUserId());
            if (!mUserOptional.isPresent()) {
                continue;
            }

            // Step 1: get all available nodes for this user according to max delay
            MUser mUser = mUserOptional.get();
            String closestNodeId = MSystemModel.getIns().getMSNManager().getClosestNodeId(mUser.getPosition());
            Optional<MServerNode> closestNodeOption = MSystemModel.getIns().getMSNManager().getById(closestNodeId);
            MServerNode closestNode = closestNodeOption.get();
            List<MServerNode> serverNodeList = MSystemModel.getIns().getMSNManager().getConnectedNodesDecentWithDelayTolerance(closestNodeId);
            serverNodeList.add(0, closestNode);

            // Step 2: try to satisfy each demand not meet
            Optional<MDemandState> demandStateOp = MSystemModel.getIns().getDemandStateManager().getById(userDemand.getId());
            MDemandState demandState = demandStateOp.get();

           // Step 2.1: try to find an exist instance
            boolean isSuccess = false;
            for (MServerNode serverNode : serverNodeList) {
                List<MServiceInstance> candidateList = serverOperator.getInstancesCanMetWithEnoughCapOnNode(serverNode.getId(), userDemand);
                if (candidateList.size() > 0) {
                    serverOperator.assignDemandToIns(userDemand, candidateList.get(0), demandState);
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
                    if (serverOperator.ifNodeHasResForIns(serverNode.getId(), bestService.getId())) {
                        String uniqueInstanceId = MIDUtils.generateInstanceId(serverNode.getId(), bestService.getId());
                        MServiceInstance newInstance = serverOperator.addNewInstance(bestService.getId(), serverNode.getId(), uniqueInstanceId);
                        serverOperator.assignDemandToIns(userDemand, newInstance, demandState);
                        isSuccess = true;
                        break;
                    }
                }
            }

            if (!isSuccess) {
                throw new RuntimeException("Demand cannot be satisfied! : " + userDemand.toString());
            }
        }

        return null;
    }

    List<MUserDemand> replaceCompositionPart(List<MUserDemand> userDemands) {
        MServerOperator operator = MSystemModel.getIns().getOperator();
        List<MUserDemand> resultList = new ArrayList<>();
        int startIndex = 0;
        Pair<MServiceInterface, Integer> r = operator.findNextSuitableComService(userDemands, startIndex);
        while (r.getValue1() <= userDemands.size()) {
            if (r.getValue0() == null) {
                resultList.add(userDemands.get(startIndex));
            } else {
                resultList.add(new MUserDemand(
                        userDemands.get(startIndex).getUserId(),
                        r.getValue0().getFunctionId(),
                        r.getValue0().getSlaLevel()
                ));
            }
            startIndex = r.getValue1();
            r = operator.findNextSuitableComService(userDemands, startIndex);
        }
        return resultList;
    }
}
