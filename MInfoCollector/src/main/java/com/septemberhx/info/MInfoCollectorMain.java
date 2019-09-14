package com.septemberhx.info;

import com.septemberhx.common.base.IInfoCollector;
import com.septemberhx.info.collectors.LogCollector.LogFileCollector;
import com.septemberhx.info.collectors.MetricsCollector.MetricsCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class MInfoCollectorMain {
    private List<IInfoCollector> collectorList;

    public MInfoCollectorMain() {
        this.collectorList = new ArrayList<>();
        this.collectorList.add(new LogFileCollector());
//        this.collectorList.add(new MetricsCollector());
    }

    public void start() {
        for (IInfoCollector iInfoCollector : this.collectorList) {
            iInfoCollector.start();
        }
    }

    public static void main(String[] args) {
        MInfoCollectorMain logCollectorMain = new MInfoCollectorMain();
        logCollectorMain.start();
    }
}
