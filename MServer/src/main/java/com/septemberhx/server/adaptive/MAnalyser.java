package com.septemberhx.server.adaptive;

import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.model.MLogChain;
import com.septemberhx.server.base.model.MUser;
import com.septemberhx.server.core.MServiceInstanceManager;
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

    public MAnalyserResult analyse(List<MServiceBaseLog> logList) {
        Map<String, MUser> userMap = new HashMap<>();

        return new MAnalyserResult();
    }

    public void analyseAvgResTimePerReqOnEachUser(Map<String, List<MServiceBaseLog>> userId2logList) {
        Map<String, List<MLogChain>> userId2ChainList = new HashMap<>();
        for (String userId : userId2logList.keySet()) {
            userId2ChainList.put(userId, this.splitLogsByEachRequestChains(userId2logList.get(userId)));
        }

        for (String userId : userId2ChainList.keySet()) {
            Long allResponseTimeInMills = 0L;
            for (MLogChain logChain : userId2ChainList.get(userId)) {
                allResponseTimeInMills += logChain.getFullResponseTime();
            }
            double avgTimePerReq = allResponseTimeInMills * 1.0 / userId2ChainList.get(userId).size();
            // todo: do the remain job here
        }
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
