package com.septemberhx.common.log;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/31
 */
@Getter
@Setter
public class MFunctionCalledLog extends MServiceBaseLog {
    private String logFromIpAddr = "";
    private Integer logFromPort = 0;

    public MFunctionCalledLog() {
        this.logType = MLogType.FUNCTION_CALL;
    }

    @Override
    protected String[] fillInfo(String[] strArr) {
        String[] leftStrArr = super.fillInfo(strArr);
        if (leftStrArr != null) {
            this.logFromIpAddr = leftStrArr[0];
            this.logFromPort = Integer.valueOf(leftStrArr[1]);
            return getUnusedStrArr(leftStrArr, 2);
        }
        return null;
    }

    @Override
    protected void fillInfo(Map<String, Object> logMap) {
        super.fillInfo(logMap);
        this.logFromIpAddr = (String) logMap.get("logFromIpAddr");
        this.logFromPort = (Integer) logMap.get("logFromPort");
    }

    @Override
    protected String uniqueLogInfo() {
        return this.concatInfo(super.uniqueLogInfo(), logFromIpAddr, logFromPort.toString());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("logFromIpAddr", this.logFromIpAddr);
        jsonObject.put("logFromPort", this.logFromPort);
        return jsonObject;
    }
}
