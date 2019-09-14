package com.septemberhx.info.collectors.LogCollector;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {

    private String logstashIp;
    private int logstashPort;

    private Logger logger = LogManager.getLogger(LogFileAlterationListenerAdaptor.class);

    public LogFileAlterationListenerAdaptor(String ip, int port) {
        this.logstashIp = ip;
        this.logstashPort = port;
    }

    @Override
    public void onFileCreate(File file) {
        logger.info(file.getName() + " created");
        Tailer.create(file, new LogFileTailerListener(this.logstashIp, this.logstashPort));
    }

    @Override
    public void onFileChange(File file) {
        logger.info(file.getName() + " changed");
    }
}
