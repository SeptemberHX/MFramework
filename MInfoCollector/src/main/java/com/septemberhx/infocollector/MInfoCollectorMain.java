package com.septemberhx.infocollector;

import com.septemberhx.infocollector.collector.LogCollector.LogFileCollector;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class MInfoCollectorMain {
    private LogFileCollector logFileCollector;

    public MInfoCollectorMain() {
        logFileCollector = new LogFileCollector();
    }

    public void start() {
        this.logFileCollector.start();
    }

    public static void main(String[] args) {
//        MInfoCollectorMain logCollectorMain = new MInfoCollectorMain();
//        logCollectorMain.start();
    }
}
