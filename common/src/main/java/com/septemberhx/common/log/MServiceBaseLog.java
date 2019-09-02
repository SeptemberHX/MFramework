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
 * @date 2019/8/26
 * @version 0.1
 *
 * Basic log type of services.
 * All the logs will be generated by the MClient module
 */
@Getter
@Setter
public abstract class MServiceBaseLog implements Comparable<MServiceBaseLog> {
    protected DateTime dateTime;  // the date time of the log
    protected String objectId;  // the id of the MObject which writes the log
    protected String methodName;  // the name of the function
    protected MLogType type;  // the type of the log

    @Override
    public int compareTo(MServiceBaseLog o) {
        return this.getDateTime().compareTo(o.getDateTime());
    }

    @Override
    public String toString() {
        String baseStr = String.format("%s|%s|%s|%s", dateTime.toString(), type.toString(), objectId, methodName);
        String uniqueInfo = this.uniqueLogInfo();
        if (uniqueInfo != null && uniqueInfo.length() != 0) {
            baseStr += "|" + uniqueInfo;
        }
        return baseStr;
    }

    private void fillBasePart(String[] strArr) {
        this.dateTime = DateTime.parse(strArr[0]);
        this.type = MLogType.valueOf(strArr[1]);
        this.objectId = strArr[2];
        this.methodName = strArr[3];
    }

    public static MServiceBaseLog getLogFromStr(String strLine) {
        String[] splitArr = strLine.split("\\|");
        if (splitArr.length < 4) {
            return null;
        }

        MServiceBaseLog baseLog = null;
        switch (MLogType.valueOf(splitArr[1])) {
            case FUNCTION_CALL:
                baseLog = new MFunctionCalledLog();
                break;
            default:
                return null;
        }

        baseLog.fillBasePart(splitArr);
        if (splitArr.length > 4) {
            baseLog.parseRemainStrArr(Arrays.copyOfRange(splitArr, 4, splitArr.length - 1));
        }
        return baseLog;
    }

    public JSONObject toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("logTimeInMills", this.dateTime.getMillis());
        jsonMap.put("logType", this.type.toString());
        jsonMap.put("logMethodName", this.methodName);
        jsonMap.put("logObjectId", this.objectId);
        this.uniqueLogInfo(jsonMap);

        return new JSONObject(jsonMap);
    }

    public void fillBasePart(Map<String, Object> logMap) {
        this.dateTime = new DateTime((long)logMap.get("logTimeInMills"));
        this.type = MLogType.valueOf((String)logMap.get("logType"));
        this.objectId = (String)logMap.get("logObjectId");
        this.methodName = (String)logMap.get("logMethodName");
    }

    public static MServiceBaseLog getLogFromMap(Map<String, Object> logMap) {
        MServiceBaseLog baseLog = null;
        try {
            switch (MLogType.valueOf((String)logMap.get("logType"))) {
                case FUNCTION_CALL:
                    baseLog = new MFunctionCalledLog();
                    break;
                default:
                    return null;
            }

            baseLog.fillBasePart(logMap);
            baseLog.parseRemainJson(logMap);
        } catch (Exception e) {
            baseLog = null;
        }
        return baseLog;
    }


    /*
      should be override by every sub class
     */

    abstract void uniqueLogInfo(Map<String, Object> infoMap);
    abstract String uniqueLogInfo();

    abstract void parseRemainJson(Map<String, Object> logMap);
    abstract void parseRemainStrArr(String[] strArr);
}