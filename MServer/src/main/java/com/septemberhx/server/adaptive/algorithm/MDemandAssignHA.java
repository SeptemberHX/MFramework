package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.utils.MIDUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static List<MBaseJob> calc(List<MUserDemand> userDemands, MServerOperator snapshotOperator) {
        int rawJobListSize = snapshotOperator.getJobList().size();

        // sort the user demands by function id and sla
        Collections.sort(userDemands, new Comparator<MUserDemand>() {
            @Override
            public int compare(MUserDemand o1, MUserDemand o2) {
                if (o1.getFunctionId().equals(o2.getFunctionId())) {
                    return -Integer.compare(o1.getSlaLevel(), o2.getSlaLevel());
                } else {
                    return o1.getFunctionId().compareTo(o2.getFunctionId());
                }
            }
        });

        // deal with all not good demands
        for (MUserDemand userDemand : userDemands) {
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
                                bestService.getId(),
                                snapshotOperator.getInstanceIdListOnNodeOfService(serverNode.getId(), bestService.getId())
                        );
                        MServiceInstance newInstance = snapshotOperator.addNewInstance(bestService.getId(), serverNode.getId(), uniqueInstanceId);
                        snapshotOperator.assignDemandToIns(userDemand, newInstance, demandState);
                        isSuccess = true;
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
