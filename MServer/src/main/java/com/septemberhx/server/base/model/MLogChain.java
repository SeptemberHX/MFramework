package com.septemberhx.server.base.model;

import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.core.MServiceInstanceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/16
 */
public class MLogChain {
    private List<MServiceBaseLog> logList;

    public MLogChain() {
        this.logList = new ArrayList<>();
    }

    public void addLog(MServiceBaseLog baseLog) {
        this.logList.add(baseLog);
    }

    private boolean isIntact() {
        if (this.logList.size() < 2) return false;

        MServiceBaseLog firstLog = this.logList.get(0);
        MServiceBaseLog lastLog = this.logList.get(this.logList.size() - 1);
        if (!MServiceInstanceManager.checkIfInstanceIsGateway(firstLog.getLogIpAddr())
                || !MServiceInstanceManager.checkIfInstanceIsGateway(lastLog.getLogIpAddr())) {
            return false;
        }

        if (firstLog.getLogType() != MLogType.FUNCTION_CALL && lastLog.getLogType() != MLogType.FUNCTION_CALL_END) {
            return false;
        }
        return true;
    }

    public long getFullResponseTime() {
        MServiceBaseLog firstLog = this.logList.get(0);
        MServiceBaseLog lastLog = this.logList.get(this.logList.size() - 1);
        return lastLog.getLogDateTime().getMillis() - firstLog.getLogDateTime().getMillis();
    }
}
