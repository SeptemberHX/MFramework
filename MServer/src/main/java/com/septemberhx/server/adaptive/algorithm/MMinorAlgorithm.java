package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

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
    @Override
    public MPlannerResult calc(MAnalyserResult data) {
        MServerOperator serverOperator = MSystemModel.getInstance().getServerOperator();
        serverOperator.reInit();

        List<EndpointPair<MSIInterface>> pCompositionList = data.getPotentialCompositionList();
        Map<String, List<MDemandState>> notMetMap = data.getAffectedUserId2MDemandStateBySla();
        Set<String> longResTimeUserIdSet = data.getAffectedUserIdByAvgTime();

        for (String userId : notMetMap.keySet()) {
            Optional<MUser> mUserOptional = MSystemModel.getInstance().getUserManager().getById(userId);
            if (!mUserOptional.isPresent()) {
                continue;
            }
            MUser mUser = mUserOptional.get();
            String closestNodeId = MSystemModel.getInstance().getMSNManager().getClosestNodeId(mUser.getPosition());
            Optional<MServerNode> closestNodeOption = MSystemModel.getInstance().getMSNManager().getById(closestNodeId);
            MServerNode closestNode = closestNodeOption.get();
            List<MServerNode> serverNodeList = MSystemModel.getInstance().getMSNManager().getAllConnectedNodesOrderedDecent(closestNodeId);
            for (MDemandState demandState : notMetMap.get(userId)) {
               MUserDemand userDemand = MSystemModel.getInstance().getUserManager()
                       .getUserDemandByUserAndDemandId(demandState.getUserId(), demandState.getInstanceId());


            }
        }

        return null;
    }
}
