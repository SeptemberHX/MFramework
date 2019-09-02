package com.septemberhx.infocollector.collector.LogCollector;

import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.infocollector.utils.LogstashUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.json.JSONObject;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogFileTailerListener implements TailerListener {

    private Tailer tailer;

    public void init(Tailer tailer) {
        this.tailer = tailer;
    }

    public void fileNotFound() {
        System.out.println(tailer.getFile().getName() + " lost!");
    }

    public void fileRotated() {

    }

    public void handle(String s) {
        /*
          todo: the time of the latest log should be recorded in each log file,
            so the old messages won't be send to logstash again after restart MInfoCollector
         */
        /*
          todo: use patterns to filter the message and only send logs we care about
         */
        MServiceBaseLog baseLog = MServiceBaseLog.getLogFromStr(s);
        if (baseLog == null) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("info", baseLog.toString());
        LogstashUtils.sendInfoToLogstash(baseLog.toString());
    }

    public void handle(Exception e) {

    }
}
