package com.septemberhx.common.log;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/31
 */
public class MFunctionCalledLog extends MServiceBaseLog {
    public MFunctionCalledLog() {
        this.type = MLogType.FUNCTION_CALL;
    }

    @Override
    String uniqueLogInfo() {
        return null;
    }

    @Override
    void parseRemainStrArr(String[] strArr) {

    }
}
