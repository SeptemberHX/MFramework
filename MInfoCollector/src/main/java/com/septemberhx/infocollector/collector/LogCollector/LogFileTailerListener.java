package com.septemberhx.infocollector.collector.LogCollector;

import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.infocollector.utils.LogstashUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogFileTailerListener implements TailerListener {

    private Tailer tailer;
    private String logstashIp;
    private int logstashPort;

    private Logger logger = LogManager.getLogger(LogFileTailerListener.class);

    public LogFileTailerListener(String logstashIp, int logstashPort) {
        this.logstashPort = logstashPort;
        this.logstashIp = logstashIp;
    }

    public void init(Tailer tailer) {
        this.tailer = tailer;
    }

    public void fileNotFound() {
        logger.warn(tailer.getFile().getName() + " lost!");
        this.tailer.stop();
    }

    public void fileRotated() {

    }

    public void handle(String s) {
        logger.debug("Tailer handles: " + s);
        MServiceBaseLog baseLog = MServiceBaseLog.getLogFromStr(s);
        if (baseLog == null) {
            logger.debug("Failed to parse: " + s + ", ignored");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mclient", baseLog.toJson());

        System.out.println(jsonObject.toString());
        LogstashUtils.sendInfoToLogstash(logstashIp, logstashPort, jsonObject.toString());
    }

    public void handle(Exception e) {

    }
}
