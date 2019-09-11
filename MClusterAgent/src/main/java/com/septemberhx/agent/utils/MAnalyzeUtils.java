package com.septemberhx.agent.utils;

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
}
