package com.septemberhx.server.adaptive;

import com.google.common.graph.*;
import com.septemberhx.common.base.MDemandChain;
import com.septemberhx.common.base.MService;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.adaptive.algorithm.ga.Configuration;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MDemandStateManager;
import com.septemberhx.server.core.MServerOperator;
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
    private static int minorTime = 0;

    @Getter
    private long timeWindowInMillis;

    private MServerOperator prevOperator;

    public MAnalyser(MServerOperator prevServerOperator) {
        this.timeWindowInMillis = 60 * 1000;
        this.prevOperator = prevServerOperator;
        System.out.println(DateTime.now().minus(this.timeWindowInMillis));
    }

    /**
     * Analyse the logs and demand states. It will not get any conclusion.
     * Only prepare data for Planner, and planner will do things to modify the system model.
     * @param logList: ALL logs from the service system
     * @param demandStates: All demand state at this time
     * @return the result of the analysing
     */
    public MAnalyserResult analyse(List<MServiceBaseLog> logList) {
        MAnalyserResult analyserResult = new MAnalyserResult();

        if (Configuration.COMPOSITION_ALL_ENABLED) {
            analyserResult.setAllCallGraph(this.buildAllCallGraph());
        }

        Map<String, List<MLogChain>> userId2LogChainList = this.analyseLogChains(logList);
        analyserResult.setDemandNotAssignedSet(this.getDemandsNotAssigned(userId2LogChainList));
        analyserResult.setCallGraph(this.buildCallGraph(userId2LogChainList));

        Map<String, Double> userId2AvgTime = this.analyseAvgResTimePerReqOnEachUser(userId2LogChainList);
        MSystemModel.getIns().getLastSystemIndex().setUserId2AvgResTimeEachReq(userId2AvgTime);

        Set<String> affectedUserIdByAvgTime = this.getUserIdWithWorseAvgTime(userId2AvgTime);
        analyserResult.setAffectedUserIdByAvgTime(affectedUserIdByAvgTime);

        Map<String, List<MUserDemand>> affectedUserId2MUserDemandsBySla = this.getUserId2MUserDemandWithWorseSla();
        affectedUserIdByAvgTime.addAll(affectedUserId2MUserDemandsBySla.keySet());
        analyserResult.setAffectedUserId2MUserDemandsBySla(affectedUserId2MUserDemandsBySla);

        // judge the type of the evolution
        MEvolveType evolveType = MEvolveType.NO_NEED;
        int userSize = MSystemModel.getIns().getUserManager().getAllValues().size();
        if (minorTime < 10 && (affectedUserIdByAvgTime.size() > MAdaptiveSystem.MINOR_THRESHOLD * userSize
                && affectedUserIdByAvgTime.size() < MAdaptiveSystem.MAJOR_THRESHOLD * userSize)
                || affectedUserIdByAvgTime.size() < 1000) {
            evolveType = MEvolveType.MINOR;
            minorTime += 1;
        } else {
            evolveType = MEvolveType.MAJOR;
            minorTime = 0;
        }
        analyserResult.setEvolveType(evolveType);

        return analyserResult;
    }

    /**
     * Build the call graph with all the logs
     *  node: interface of specific service instance
     *  edge: call direction and frequency
     * @param userId2LogChainList: All the service log chains of the affected users
     */
    private MutableValueGraph<MSInterface, Integer> buildCallGraph(Map<String, List<MLogChain>> userId2LogChainList) {
        MutableValueGraph<MSInterface, Integer> interfaceGraph = ValueGraphBuilder.directed().build();

        for (String userId : userId2LogChainList.keySet()) {
            List<MLogChain> chainList = userId2LogChainList.get(userId);
            for (MLogChain logChain : chainList) {
                List<MSInterface> sInsInterfaceList = logChain.getConnections();
                for (MSInterface serviceInstanceInterface : sInsInterfaceList) {
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
        return interfaceGraph;
    }

    /**
     * Build the call graph according to previous system status
     */
    private MutableValueGraph<MSInterface, Integer> buildAllCallGraph() {
        MutableValueGraph<MSInterface, Integer> interfaceGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        // consider before status
        for (MUser user : MSystemModel.getIns().getUserManager().getAllValues()) {
            for (MDemandChain demandChain : user.getDemandChainList()) {
                MSInterface prevInterface = null;
                for (MUserDemand userDemand : demandChain.getDemandList()) {
                    Optional<MDemandState> demandStateOptional = this.prevOperator.getDemandStateManager().getById(userDemand.getId());
                    if (!demandStateOptional.isPresent()) {
                        continue;
                    }

                    MDemandState demandState = demandStateOptional.get();
                    MSInterface MSInterface = new MSInterface(
                            demandState.getInterfaceId(),
                            this.prevOperator.getInstanceById(demandState.getInstanceId()).getServiceId()
                    );

                    if (prevInterface != null) {
                        Optional<Integer> connectCount = interfaceGraph.edgeValue(prevInterface, MSInterface);
                        int r = connectCount.orElse(0);
                        ++r;
                        if (prevInterface == MSInterface) {  // self-loop is not allowed
                            continue;
                        }
                        interfaceGraph.putEdgeValue(prevInterface, MSInterface, r);
                    }
                    prevInterface = MSInterface;
                }
            }
        }

        // consider now status
        for (MUser user : MSystemModel.getIns().getUserManager().getAllValues()) {
            for (MDemandChain demandChain : user.getDemandChainList()) {
                MSInterface prevInterface = null;
                for (MUserDemand userDemand : demandChain.getDemandList()) {
                    if (userDemand.getServiceId() == null) {
                        prevInterface = null;
                        continue;
                    }

                    // to reduce create useless chain, we will only composite the highest rid
                    // so the composite one will fit all sla levels
                    List<MService> serviceList = MSystemModel.getIns().getServiceManager().getAllServicesByServiceName(userDemand.getServiceId());
                    serviceList.sort(Comparator.comparing(MService::getRId));
                    MService tService = serviceList.get(serviceList.size() - 1);
                    MSInterface msInterface = new MSInterface(
                        tService.getInterfaceMetUserDemand(userDemand).get(0).getInterfaceId(),
                        tService.getId()
                    );

                    if (prevInterface != null) {
                        Optional<Integer> connectCount = interfaceGraph.edgeValue(prevInterface, msInterface);
                        int r = connectCount.orElse(0);
                        ++r;
                        interfaceGraph.putEdgeValue(prevInterface, msInterface, r);
                    }
                    prevInterface = msInterface;
                }
            }
        }
        return interfaceGraph;
    }

    private Map<String, List<String>> getDemandsNotAssigned(Map<String, List<MLogChain>> userId2LogChainListMap) {
        Map<String, List<String>> result = new HashMap<>();
        for (String userId : userId2LogChainListMap.keySet()) {
            for (MLogChain logChain : userId2LogChainListMap.get(userId)) {
                if (!result.containsKey(userId)) {
                    result.put(userId, new ArrayList<>());
                }
                result.get(userId).addAll(logChain.getNotMetDemandIdList());
            }
        }
        return result;
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
            Collections.sort(userId2logList.get(userId), Comparator.comparing(MBaseLog::getLogDateTime));
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

    private Map<String, List<MUserDemand>> getUserId2MUserDemandWithWorseSla() {
        MDemandStateManager demandStateManager = MSystemModel.getIns().getDemandStateManager();
        List<MUser> currUserList = MSystemModel.getIns().getUserManager().getAllValues();
        Map<String, List<MUserDemand>> userId2MUserDemands = new HashMap<>();
        for (MUser user : currUserList) {
            for (MUserDemand demand : user.getAllDemands()) {
                if (!demandStateManager.containsById(demand.getId()) ||
                    !MDemandStateManager.checkIfDemandSatisfied(demandStateManager.getById(demand.getId()).get())) {
                    if (!userId2MUserDemands.containsKey(user.getId())) {
                        userId2MUserDemands.put(user.getId(), new ArrayList<>());
                    }
                    userId2MUserDemands.get(user.getId()).add(demand);
                }
            }
        }
        return userId2MUserDemands;
    }

    private List<MLogChain> splitLogsByEachRequestChains(List<MServiceBaseLog> logList) {
        List<MLogChain> logChainList = new ArrayList<>();
        MLogChain currLogChain = new MLogChain();
        for (MServiceBaseLog serviceLog : logList) {
            if (MServiceInstanceManager.checkIfInstanceIsGateway(serviceLog.getLogObjectId())) {
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
}
