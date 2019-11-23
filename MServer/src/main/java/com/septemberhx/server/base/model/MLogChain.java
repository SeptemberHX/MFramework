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

        long executingTimeInMillis = 0;
        for (int i = 1; i < this.logList.size() - 1; i+=1) {
            if (this.logList.get(i).getLogType() == MLogType.FUNCTION_CALL &&
                    this.logList.get(i + 1).getLogType() == MLogType.FUNCTION_CALL_END &&
                    this.logList.get(i).getLogObjectId().equals(this.logList.get(i + 1).getLogObjectId())) {
                executingTimeInMillis = this.logList.get(i + 1).getLogDateTime().getMillis() - this.logList.get(i).getLogDateTime().getMillis();
            }
        }

        return lastLog.getLogDateTime().getMillis() - firstLog.getLogDateTime().getMillis() - executingTimeInMillis;
    }

    public List<String> getNotMetDemandIdList() {
        List<String> demandIdList = new ArrayList<>();
        for (int i = 0; i < this.logList.size() - 1; ++i) {
            if (this.logList.get(i).getLogType() == MLogType.FUNCTION_CALL &&
                MServiceInstanceManager.checkIfInstanceIsGateway(this.logList.get(i).getLogObjectId()) &&
                MServiceInstanceManager.checkIfInstanceIsGateway(this.logList.get(i+1).getLogObjectId())) {
                demandIdList.add(this.logList.get(i).getLogMethodName());
            }
        }

        return demandIdList;
    }

    public List<MSInterface> getConnections() {
        List<MSInterface> instanceInterfaces = new ArrayList<>();
        for (MServiceBaseLog serviceBaseLog : this.logList) {
            if (serviceBaseLog.getLogType() != MLogType.FUNCTION_CALL) {
                continue;
            }

            Optional<MServiceInstance> serviceInstance = MSystemModel.getIns().getMSIManager().getInstanceByIpAddr(serviceBaseLog.getLogIpAddr());
            if (serviceInstance.isPresent()) {
                MSInterface instanceInterface = new MSInterface(
                        serviceBaseLog.getLogMethodName(),
                        serviceBaseLog.getLogObjectId()
                );
                instanceInterfaces.add(instanceInterface);
            }
        }
        return instanceInterfaces;
    }
}
