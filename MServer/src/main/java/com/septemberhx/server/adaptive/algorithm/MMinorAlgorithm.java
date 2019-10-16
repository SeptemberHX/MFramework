package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBaseJob;
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
        // First, init operator
        MServerOperator serverOperator = MSystemModel.getIns().getOperator();
        serverOperator.reInit();
        // Then, do the composition job behind initialization. It will modify system model by operator
        MCompositionAlgorithmInCommon.doCompositionPart(data.getCallGraph());
        // DO NOT CHANGE THE ORDER ABOVE.

        // Below, do the calc job
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

        // collect all user demands that have no status
        for (MUser user : MSystemModel.getIns().getUserManager().getAllValues()) {
            demandList.addAll(serverOperator.filterNotIn(user.getAllDemands()));
        }

        // deal with all not good demands
        MDemandAssignHA.calc(demandList, serverOperator);
        serverOperator.printStatus();

        MPlannerResult plannerResult = new MPlannerResult();
        plannerResult.addJobs(serverOperator.getJobList());
        return plannerResult;
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
