package com.septemberhx.server.adaptive;

import com.google.common.graph.*;
import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MLogChain;
import com.septemberhx.server.base.model.MSIInterface;
import com.septemberhx.server.base.model.MSystemIndex;
import com.septemberhx.server.core.MDemandStateManager;
import com.septemberhx.server.core.MServiceInstanceManager;
import com.septemberhx.server.core.MSystemModel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;


/**
 * Analyse the input data to provider useful information for next step.
 * The result will be constructed as a MAnalyserOutput object
 */
public class MAnalyser {

    private static Logger logger = LogManager.getLogger(MAnalyser.class);

    @Getter
    private long timeWindowInMillis;

    public MAnalyser() {
        this.timeWindowInMillis = 60 * 1000;
        System.out.println(DateTime.now().minus(this.timeWindowInMillis));
    }

    public MAnalyserResult analyse(List<MServiceBaseLog> logList, List<MDemandState> demandStates) {
        MAnalyserResult analyserResult = new MAnalyserResult();
        Map<String, List<MLogChain>> userId2LogChainList = this.analyseLogChains(logList);
        analyserResult.setPotentialCompositionList(this.analyseMostUsedCombination(userId2LogChainList));

        Map<String, Double> userId2AvgTime = this.analyseAvgResTimePerReqOnEachUser(userId2LogChainList);
        Set<String> affectedUserIdByAvgTime = this.getUserIdWithWorseAvgTime(userId2AvgTime);
        analyserResult.setAffectedUserIdByAvgTime(affectedUserIdByAvgTime);

        Map<String, List<MDemandState>> affectedUserId2MDemandStateBySla = this.getUserId2MDemandStateWithWorseSla(demandStates);
        affectedUserIdByAvgTime.addAll(affectedUserId2MDemandStateBySla.keySet());
        analyserResult.setAffectedUserId2MDemandStateBySla(affectedUserId2MDemandStateBySla);

        // judge the type of the evolution
        MEvolveType evolveType = MEvolveType.NO_NEED;
        if (affectedUserIdByAvgTime.size() > MAdaptiveSystem.MINOR_THRESHOLD
                && affectedUserIdByAvgTime.size() < MAdaptiveSystem.MAJOR_THRESHOLD) {
            evolveType = MEvolveType.MINOR;
        } else {
            evolveType = MEvolveType.MAJOR;
        }
        analyserResult.setEvolveType(evolveType);

        return analyserResult;
    }

    /**
     * We are trying to find the most common used service combinations here. And abandon combinations are also concerned.
     * Only concern the users whose service quality is decreasing !!!
     *  First we will try to find the top-K service combinations;
     *  Then we will compare it to the service composition instances we have already;
     *  Finally we will decide whether we need build a new composition service or remove old composition instance;
     * @param userId2LogChainList: All the service log chains of the affected users
     */
    private List<EndpointPair<MSIInterface>> analyseMostUsedCombination(Map<String, List<MLogChain>> userId2LogChainList) {
        MutableValueGraph<MSIInterface, Integer> interfaceGraph = ValueGraphBuilder.directed().build();

        for (String userId : userId2LogChainList.keySet()) {
            List<MLogChain> chainList = userId2LogChainList.get(userId);
            for (MLogChain logChain : chainList) {
                List<MSIInterface> sInsInterfaceList = logChain.getConnections();
                for (MSIInterface serviceInstanceInterface : sInsInterfaceList) {
                    interfaceGraph.addNode(serviceInstanceInterface);
                }

                for (int i = 0; i < sInsInterfaceList.size(); ++i) {
                    if (i < sInsInterfaceList.size() - 1) {
                        Optional<Integer> connectTime = interfaceGraph.edgeValue(sInsInterfaceList.get(i), sInsInterfaceList.get(i + 1));
                        int r = 0;
                        if (connectTime.isPresent()) {
                            r = connectTime.get();
                        }
                        ++r;
                        interfaceGraph.putEdgeValue(sInsInterfaceList.get(i), sInsInterfaceList.get(i + 1), r);
                    }
                }
            }
        }

        List<EndpointPair<MSIInterface>> edgeList = new ArrayList<>(interfaceGraph.edges());
        Collections.sort(edgeList, (o1, o2) ->
                -interfaceGraph.edgeValueOrDefault(o1, 0).compareTo(interfaceGraph.edgeValueOrDefault(o2, 0)));
        int callChainCount = 0;
        for (String userId : userId2LogChainList.keySet()) {
            callChainCount += userId2LogChainList.get(userId).size();
        }

        List<EndpointPair<MSIInterface>> resultList = new ArrayList<>();
        for (EndpointPair<MSIInterface> edge : edgeList) {
            if (interfaceGraph.edgeValueOrDefault(edge, 0) > callChainCount * MAdaptiveSystem.COMPOSITION_THRESHOLD) {
                resultList.add(edge);
            } else {
                break;
            }
        }
        return resultList;
    }

    private Map<String, List<MLogChain>> analyseLogChains(List<MServiceBaseLog> logList) {
        Map<String, List<MServiceBaseLog>> userId2logList = new HashMap<>();
        for (MServiceBaseLog baseLog : logList) {
            if (!userId2logList.containsKey(baseLog.getLogUserId())) {
                userId2logList.put(baseLog.getLogUserId(), new ArrayList<>());
            }
            userId2logList.get(baseLog.getLogUserId()).add(baseLog);
        }
        for (String userId : userId2logList.keySet()) {
            Collections.sort(userId2logList.get(userId));
        }

        Map<String, List<MLogChain>> userId2ChainList = new HashMap<>();
        for (String userId : userId2logList.keySet()) {
            userId2ChainList.put(userId, this.splitLogsByEachRequestChains(userId2logList.get(userId)));
        }
        return userId2ChainList;
    }

    private Map<String, Double> analyseAvgResTimePerReqOnEachUser(Map<String, List<MLogChain>> userId2ChainList) {
        Map<String, Double> userId2AvgTime = new HashMap<>();
        for (String userId : userId2ChainList.keySet()) {
            Long allResponseTimeInMills = 0L;
            for (MLogChain logChain : userId2ChainList.get(userId)) {
                allResponseTimeInMills += logChain.getFullResponseTime();
            }
            double avgTimePerReq = allResponseTimeInMills * 1.0 / userId2ChainList.get(userId).size();
            userId2AvgTime.put(userId, avgTimePerReq);
        }
        return userId2AvgTime;
    }

    private Set<String> getUserIdWithWorseAvgTime(Map<String, Double> userId2AvgTime) {
        MSystemIndex lastSystemIndex = MSystemModel.getIns().getLastSystemIndex();
        Set<String> userIdNeedAdjustSet = new HashSet<>();

        for (String userId : userId2AvgTime.keySet()) {
            if (lastSystemIndex.getUserId2AvgResTimeEachReq().containsKey(userId)
                    && lastSystemIndex.getUserId2AvgResTimeEachReq().get(userId) < userId2AvgTime.get(userId)) {
                userIdNeedAdjustSet.add(userId);
            }
        }
        return userIdNeedAdjustSet;
    }

    private Map<String, List<MDemandState>> getUserId2MDemandStateWithWorseSla(List<MDemandState> demandStates) {
        Map<String, List<MDemandState>> userId2MDemandState = new HashMap<>();
        for (MDemandState demandState : demandStates) {
            if (!MDemandStateManager.checkIfDemandSatisfied(demandState)) {
                if (!userId2MDemandState.containsKey(demandState.getUserId())) {
                    userId2MDemandState.put(demandState.getUserId(), new ArrayList<>());
                }
                userId2MDemandState.get(demandState.getUserId()).add(demandState);
            }
        }
        return userId2MDemandState;
    }

    private List<MLogChain> splitLogsByEachRequestChains(List<MServiceBaseLog> logList) {
        List<MLogChain> logChainList = new ArrayList<>();
        MLogChain currLogChain = new MLogChain();
        for (MServiceBaseLog serviceLog : logList) {
            if (MServiceInstanceManager.checkIfInstanceIsGateway(serviceLog.getLogIpAddr())) {
                if (serviceLog.getLogType() == MLogType.FUNCTION_CALL) {
                    currLogChain = new MLogChain();
                    currLogChain.addLog(serviceLog);
                } else if (serviceLog.getLogType() == MLogType.FUNCTION_CALL_END) {
                    currLogChain.addLog(serviceLog);
                    logChainList.add(currLogChain);
                }
            } else {
                currLogChain.addLog(serviceLog);
            }
        }
        return logChainList;
    }

    public static void main(String[] args) {
        MAnalyser a = new MAnalyser();
    }
}
