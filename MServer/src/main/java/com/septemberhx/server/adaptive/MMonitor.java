package com.septemberhx.server.adaptive;

import com.septemberhx.common.log.MServiceBaseLog;
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
        if (this.latestLogDateTime == null || this.latestLogDateTime.isBefore(serviceLog.getDateTime())) {
            this.latestLogDateTime = serviceLog.getDateTime();
        }
        this.logList.add(serviceLog);
    }

    public List<MServiceBaseLog> getLogBetweenDateTime(DateTime startTime, DateTime endTime) {
        List<MServiceBaseLog> resultLogList = new ArrayList<>();
        for (MServiceBaseLog log : this.logList) {
            if (log.getDateTime().isEqual(startTime) || log.getDateTime().isAfter(startTime)
                    && log.getDateTime().isBefore(endTime)) {
                resultLogList.add(log);
            }
        }

        Collections.sort(resultLogList);
        return resultLogList;
    }
}
