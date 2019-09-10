package com.septemberhx.common.utils;

import com.septemberhx.common.log.MFunctionCalledLog;
import com.septemberhx.common.log.MLogType;
import com.septemberhx.common.log.MServiceBaseLog;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.joda.time.DateTime;


/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/31
 */
public class MLogUtils {
    public static Logger logger = createLogger();

    public static String convertLogObjectToString(MServiceBaseLog baseLog) {
        return baseLog.toString();
    }

    public static MServiceBaseLog getLogObjectFromString(String formattedStr) {
        return MServiceBaseLog.getLogFromStr(formattedStr);
    }

    private static Logger createLogger() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext();
        final Configuration config = ctx.getConfiguration();
        String fileName = config.
                getStrSubstitutor().replace("/var/log/mclient/file-with-date-${date:yyyy-MM-dd}.log");

        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(ctx.getConfiguration())
                .withPattern("%m%n").build();

        Appender fileAppender = FileAppender.newBuilder()
                .setLayout(layout)
                .withFileName(fileName)
                .setName("pattern")
                .build();
        fileAppender.start();

        Appender consoleAppender =  ConsoleAppender.createAppender(layout, null, null, "CONSOLE_APPENDER", null, null);
        consoleAppender.start();

        AppenderRef ref= AppenderRef.createAppenderRef("CONSOLE_APPENDER",null,null);
        AppenderRef ref2 = AppenderRef.createAppenderRef("FILE_APPENDER", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref, ref2};
        LoggerConfig loggerConfig= LoggerConfig.createLogger("false", Level.INFO,"CONSOLE_LOGGER","com",refs,null,config,null);
        loggerConfig.addAppender(consoleAppender,null,null);
        loggerConfig.addAppender(fileAppender, null, null);

        config.addAppender(consoleAppender);
        config.addLogger("com", loggerConfig);
        ctx.updateLoggers(config);

        return LogManager.getContext().getLogger("com");
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
