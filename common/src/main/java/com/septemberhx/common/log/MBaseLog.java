package com.septemberhx.common.log;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public abstract class MBaseLog implements Comparable<MBaseLog> {
    protected DateTime logDateTime;
    protected MLogType logType;

    @Override
    public int compareTo(MBaseLog o) {
        return this.logDateTime.compareTo(o.logDateTime);
    }

    @Override
    public String toString() {
        return this.uniqueLogInfo();
    }

    public static JSONObject convertLog2JsonObejct(MBaseLog baseLog) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mclient", baseLog.toJson());
        return jsonObject;
    }

    /**
     * Please override this functions for subclass
     * @return
     */
    protected String uniqueLogInfo() {
        return this.concatInfo(logDateTime.toString(), logType.toString());
    }

    /**
     * Please override this functions for subclass
     * @param strArr: Array of String to parse
     * @return The unused part of strArr
     */
    protected String[] fillInfo(String[] strArr) {
        this.logDateTime = DateTime.parse(strArr[0]);
        this.logType = MLogType.valueOf(strArr[1]);
        return this.getUnusedStrArr(strArr, 2);
    }

    /**
     * Please override this functions for subclass
     * @param logMap: the log info map
     */
    protected void fillInfo(Map<String, Object> logMap) {
        this.logDateTime = DateTime.parse((String) logMap.get("logDateTimeInMills"));
        this.logType = MLogType.valueOf((String) logMap.get("logType"));
    }

    public static MBaseLog getLogFromStr(String strLine) {
        String[] splitArr = strLine.split("\\|");
        if (splitArr.length < 2) {
            return null;
        }

        MBaseLog baseLog = getPlainBaseLogByType(MLogType.valueOf(splitArr[1]));
        baseLog.fillInfo(splitArr);
        return baseLog;
    }

    public static MBaseLog getLogFromMap(Map<String, Object> logMap) {
        MBaseLog baseLog = null;
        try {
            baseLog = getPlainBaseLogByType(MLogType.valueOf((String) logMap.get("logType")));
            baseLog.fillInfo(logMap);
        } catch (Exception e) {
            baseLog = null;
        }
        return baseLog;
    }

    private static MBaseLog getPlainBaseLogByType(MLogType logType) {
        MBaseLog baseLog = null;
        switch (logType) {
            case NODE_METRICS_LOG:
                baseLog = new MNodeMetricsLog();
                break;
            case CONTAINER_METRICS_LOG:
                baseLog = new MDockerMetricsLog();
                break;
            case FUNCTION_CALL_END:
                baseLog = new MFunctionCallEndLog();
                break;
            case FUNCTION_CALL:
                baseLog = new MFunctionCalledLog();
                break;
            default:
                return null;
        }
        return baseLog;
    }

    /**
     * Please override this function in subclass
     * @return JSONObject
     */
    public JSONObject toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("logDateTimeInMills", logDateTime.getMillis());
        jsonMap.put("logType", logType);
        return new JSONObject(jsonMap);
    }

    String concatInfo(String... infoStrs) {
        return String.join("|", infoStrs);
    }

    String[] getUnusedStrArr(String[] strArr, int fromIndex) {
        if (strArr.length > fromIndex) {
            return Arrays.copyOfRange(strArr, fromIndex, strArr.length);
        } else {
            return null;
        }
    }
}
