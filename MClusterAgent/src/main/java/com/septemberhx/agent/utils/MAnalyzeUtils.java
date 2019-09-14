package com.septemberhx.agent.utils;

import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MMetricsBaseLog;
import com.septemberhx.common.log.MServiceBaseLog;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/10
 */
public class MAnalyzeUtils {

    /**
     * Re-construct the logs by userId and their timestamp
     * @param logList: MServiceBaseLog list
     * @return Re-construct log map
     */
    public static Map<String, List<MServiceBaseLog>> rebuildCallChain(List<MServiceBaseLog> logList) {
        Map<String, List<MServiceBaseLog>> userId2LogList = new HashMap<>();
        for (MServiceBaseLog log : logList) {
            if (!userId2LogList.containsKey(log.getLogUserId())) {
                userId2LogList.put(log.getLogUserId(), new ArrayList<>());
            }

            userId2LogList.get(log.getLogUserId()).add(log);
        }
        for (String userId : userId2LogList.keySet()) {
            Collections.sort(userId2LogList.get(userId));
        }
        return userId2LogList;
    }

    public static Map<String, List<MMetricsBaseLog>> rebuildMetricsLogs(List<MMetricsBaseLog> metricsBaseLogs) {
        Map<String, List<MMetricsBaseLog>> ipAddr2MetricsLogList = new HashMap<>();
        for (MMetricsBaseLog log : metricsBaseLogs) {
            if (!ipAddr2MetricsLogList.containsKey(log.getLogHostname())) {
                ipAddr2MetricsLogList.put(log.getLogHostname(), new ArrayList<>());
            }
            ipAddr2MetricsLogList.get(log.getLogHostname()).add(log);
        }
        for (String ipAddr : ipAddr2MetricsLogList.keySet()) {
            Collections.sort(ipAddr2MetricsLogList.get(ipAddr));
        }
        return ipAddr2MetricsLogList;
    }

    public static List<MServiceBaseLog> getServiceBaseLog(List<MBaseLog> logList) {
        List<MServiceBaseLog> resultList = new ArrayList<>();
        for (MBaseLog log : logList) {
            if (log instanceof MServiceBaseLog) {
                resultList.add((MServiceBaseLog) log);
            }
        }

        return resultList;
    }

    public static List<MMetricsBaseLog> getMetricsBaseLog(List<MBaseLog> logList) {
        List<MMetricsBaseLog> resultList = new ArrayList<>();
        for (MBaseLog log : logList) {
            if (log instanceof MMetricsBaseLog) {
                resultList.add((MMetricsBaseLog) log);
            }
        }

        return resultList;
    }

    public static void preprocessLog(List<MBaseLog> logList) {
        List<MServiceBaseLog> serviceBaseLogs = MAnalyzeUtils.getServiceBaseLog(logList);
        List<MMetricsBaseLog> metricsBaseLogs = MAnalyzeUtils.getMetricsBaseLog(logList);

        Map<String, List<MServiceBaseLog>> userId2LogList = MAnalyzeUtils.rebuildCallChain(serviceBaseLogs);
        Map<String, List<MMetricsBaseLog>> ipAddr2MetricsLogList = MAnalyzeUtils.rebuildMetricsLogs(metricsBaseLogs);
    }
}
