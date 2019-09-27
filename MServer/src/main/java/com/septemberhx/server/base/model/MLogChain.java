package com.septemberhx.server.base.model;

import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.core.MServiceInstanceManager;
import com.septemberhx.server.core.MSystemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<MSIInterface> getConnections() {
        List<MSIInterface> instanceInterfaces = new ArrayList<>();
        for (MServiceBaseLog serviceBaseLog : this.logList) {
            if (serviceBaseLog.getLogType() != MLogType.FUNCTION_CALL) {
                continue;
            }

            Optional<MServiceInstance> serviceInstance = MSystemModel.getIns().getMSIManager().getInstanceByIpAddr(serviceBaseLog.getLogIpAddr());
            if (serviceInstance.isPresent()) {
                MSIInterface instanceInterface = new MSIInterface(
                        serviceInstance.get().getId(),
                        serviceBaseLog.getLogObjectId(),
                        serviceBaseLog.getLogMethodName()
                );
                instanceInterfaces.add(instanceInterface);
            }
        }
        return instanceInterfaces;
    }
}
