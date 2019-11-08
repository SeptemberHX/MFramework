package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MDeployJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public abstract class MBaseGA {
    static List<String> fixedNodeIdList;
    static List<String> fixedServiceIdList;
    static Map<String, Integer> fixedNodeId2Index;
    static Map<String, Integer> fixedServiceId2Index;

    static double minValue1 = 0;
    static double maxValue1 = 0;
    static double minValue2 = 0;
    static double maxValue2 = 0;

    MServerOperator rawOperator;

    Logger logger = LogManager.getLogger(this.getClass());

    MPopulation population;

    public MBaseGA(MServerOperator snapshotOperator, MServerOperator rawOperator) {
        this.rawOperator = rawOperator;
        fixedNodeIdList = new ArrayList<>();
        fixedServiceIdList = new ArrayList<>();

        // the cloud node will not take part in the solution due to the unlimited resource
        MSystemModel.getIns().getMSNManager().getFixedOrderNodeList().forEach(n -> {
//                if (n.getNodeType() != ServerNodeType.CLOUD) {
                    fixedNodeIdList.add(n.getId());
//                }
            }
        );
        snapshotOperator.getServiceManager().getFixedServiceList().forEach(s -> fixedServiceIdList.add(s.getId()));

        fixedNodeId2Index = new HashMap<>();
        for (int i = 0; i < fixedNodeIdList.size(); ++i) {
            fixedNodeId2Index.put(fixedNodeIdList.get(i), i);
        }

        fixedServiceId2Index = new HashMap<>();
        for (int i = 0; i < fixedServiceIdList.size(); ++i) {
            fixedServiceId2Index.put(fixedServiceIdList.get(i), i);
        }

        // calculate min, max values of cost and fitness
        List<Integer> userCapList = snapshotOperator.getAllServices().stream().map(MService::getMaxUserCap).collect(Collectors.toList());
        int minCap = Collections.min(userCapList);
        int demandSize = MSystemModel.getIns().getUserManager().getAllUserDemands().size();
        minValue1 = 0;
        maxValue1 = (demandSize * 1.0 / minCap) * MBaseJob.COST_DEPLOY + demandSize * MBaseJob.COST_SWITCH + rawOperator.getAllInstances().size() * MBaseJob.COST_REMOVE;

        double maxInSizeDate = snapshotOperator.getServiceManager().getMaxInSizeData();
        double maxOutSizeDate = snapshotOperator.getServiceManager().getMaxOutSizeData();
        double maxBandWidth = MSystemModel.getIns().getMSNManager().getMaxBandwidth();
        double maxDelay = MSystemModel.getIns().getMSNManager().getMaxDelay();
        double minDelay = MSystemModel.getIns().getMSNManager().getMinDelayBetweenNodeAndUser();
        double maxTran = (maxInSizeDate + maxOutSizeDate) / maxBandWidth;
        double allMaxScore = 0;
        double allMinScore = 0;
        double allChainCount = 0;
        for (MUser user : MSystemModel.getIns().getUserManager().getAllValues()) {
            for (MDemandChain demandChain : user.getDemandChainList()) {
                allMaxScore += demandChain.getDemandList().size() * (MAdaptiveSystem.ALPHA * maxTran + (1 - MAdaptiveSystem.ALPHA) * maxDelay);
                allMinScore += demandChain.getDemandList().size() * (MAdaptiveSystem.ALPHA * 0 + (1 - MAdaptiveSystem.ALPHA) * minDelay);
            }
            allChainCount += user.getDemandChainList().size();
        }
        allMaxScore /= allChainCount;
        allMinScore /= allChainCount;
        minValue1 = allMinScore;
        maxValue2 = allMaxScore;
    }

    public abstract MServerOperator evolve();
}
