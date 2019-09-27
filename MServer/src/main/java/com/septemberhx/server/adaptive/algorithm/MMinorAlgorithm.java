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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        // todo: analyze demand chain to replace old services with new composition one

        for (String userId : notMetMap.keySet()) {
            Optional<MUser> mUserOptional = MSystemModel.getIns().getUserManager().getById(userId);
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
            for (MDemandState demandState : notMetMap.get(userId)) {
                MUserDemand userDemand = MSystemModel.getIns().getUserManager()
                        .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getInstanceId());

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
        }

        return null;
    }
}
