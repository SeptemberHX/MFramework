package com.septemberhx.server.adaptive;

import com.septemberhx.common.base.MUser;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.base.MAnalyserInput;
import com.septemberhx.server.utils.MServerUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MMonitor {

    private List<MServiceBaseLog> logList;
    private DateTime latestLogDateTime;

    public MMonitor() {
        this.logList = new ArrayList<>();
        this.latestLogDateTime = null;
    }

    /**
     * Pass the service log into the monitor
     * @param serviceLog
     */
    public void acceptLog(MServiceBaseLog serviceLog) {
        if (this.latestLogDateTime == null || this.latestLogDateTime.isBefore(serviceLog.getLogDateTime())) {
            this.latestLogDateTime = serviceLog.getLogDateTime();
        }
        this.logList.add(serviceLog);
    }

    public List<MServiceBaseLog> getLogBetweenDateTime(DateTime startTime, DateTime endTime) {
        List<MServiceBaseLog> resultLogList = new ArrayList<>();
        for (MServiceBaseLog log : this.logList) {
            if (log.getLogDateTime().isEqual(startTime) || log.getLogDateTime().isAfter(startTime)
                    && log.getLogDateTime().isBefore(endTime)) {
                resultLogList.add(log);
            }
        }

        Collections.sort(resultLogList);
        return resultLogList;
    }

    public List<MBaseLog> fetchLogFromCluster(DateTime startTime, DateTime endTime) {
        List<MBaseLog> logList = new ArrayList<>();
        for (String logStr : MServerUtils.fetchClusterLogsByDatetime(startTime, endTime)) {
            logList.add(MBaseLog.getLogFromStr(logStr));
        }
        return logList;
    }

    public List<MUser> fetchUserInfoFromCluster() {
        return MServerUtils.fetchClusterUsers();
    }

    public MAnalyserInput monitor(DateTime startTime, DateTime endTime) {
        List<MBaseLog> logList = this.fetchLogFromCluster(startTime, endTime);
        List<MUser> userList = this.fetchUserInfoFromCluster();
        return new MAnalyserInput(userList, logList);
    }
}
