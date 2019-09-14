package com.septemberhx.info;

import com.septemberhx.info.collectors.MDockerMetricsCollector;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/14
 */
public class MDockerCollectorMain {
    public static void main(String[] args) {
        MDockerMetricsCollector dockerMetricsCollector = new MDockerMetricsCollector();
        dockerMetricsCollector.start();
    }
}
