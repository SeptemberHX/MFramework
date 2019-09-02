package com.septemberhx.common.utils;

import com.septemberhx.common.log.MFunctionCalledLog;
import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/31
 */
public class MLogUtils {
    public static Logger logger = LogManager.getLogger(MLogUtils.class);

    public static String convertLogObjectToString(MServiceBaseLog baseLog) {
        return baseLog.toString();
    }

    public static MServiceBaseLog getLogObjectFromString(String formattedStr) {
        return MServiceBaseLog.getLogFromStr(formattedStr);
    }

    public static void log(MServiceBaseLog baseLog) {
        logger.info(baseLog);
    }

    public static void main(String[] args) {
        MFunctionCalledLog testLog = new MFunctionCalledLog();
        testLog.setDateTime(DateTime.now());
        testLog.setType(MLogType.FUNCTION_CALL);
        testLog.setObjectId("123-321-123-231");
        testLog.setMethodName("test");

        String str = MLogUtils.convertLogObjectToString(testLog);
        System.out.println(str);
        MLogUtils.log(testLog);

        MServiceBaseLog log = MLogUtils.getLogObjectFromString(str);
        System.out.println(log);
    }
}
