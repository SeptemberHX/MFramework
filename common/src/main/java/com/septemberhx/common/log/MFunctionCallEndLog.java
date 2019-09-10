package com.septemberhx.common.log;

import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/31
 */
public class MFunctionCallEndLog extends MServiceBaseLog {
    public MFunctionCallEndLog() {
        this.type = MLogType.FUNCTION_CALL_END;
    }

    @Override
    String uniqueLogInfo() {
        return null;
    }

    @Override
    void parseRemainStrArr(String[] strArr) {

    }

    @Override
    void uniqueLogInfo(Map<String, Object> infoMap) {

    }

    @Override
    public void fillBasePart(Map<String, Object> jsonObject) {
        super.fillBasePart(jsonObject);
    }

    @Override
    void parseRemainJson(Map<String, Object> jsonObject) {

    }
}
