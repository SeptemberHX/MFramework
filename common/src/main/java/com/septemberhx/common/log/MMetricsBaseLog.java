package com.septemberhx.common.log;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/9
 */
@Getter
@Setter
public class MMetricsBaseLog extends MBaseLog {
    protected String logIpAddr;
    protected Long logCpuUsage;
    protected Long logRamUsage;

    public MMetricsBaseLog() {
        this.logType = MLogType.METRICS_LOG;
    }

    @Override
    protected String uniqueLogInfo() {
        return this.concatInfo(super.uniqueLogInfo(), logIpAddr, logCpuUsage.toString(), logRamUsage.toString());
    }

    @Override
    protected String[] fillInfo(String[] strArr) {
        String[] leftStrArr = super.fillInfo(strArr);
        if (leftStrArr != null) {
            this.logIpAddr = leftStrArr[0];
            this.logCpuUsage = Long.decode(leftStrArr[1]);
            this.logRamUsage = Long.decode(leftStrArr[2]);
            return this.getUnusedStrArr(leftStrArr, 3);
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("logIpAddr", logIpAddr);
        jsonObject.put("logCpuUsage", logCpuUsage);
        jsonObject.put("logRamUsage", logRamUsage);
        return jsonObject;
    }

    @Override
    protected void fillInfo(Map<String, Object> logMap) {
        super.fillInfo(logMap);
        this.logIpAddr = (String) logMap.get("logIpAddr");
        this.logCpuUsage = Long.decode((String) logMap.get("logCpuUsage"));
        this.logRamUsage = Long.decode((String) logMap.get("logRamUsage"));
    }
}
