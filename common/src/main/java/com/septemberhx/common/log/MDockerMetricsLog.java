package com.septemberhx.common.log;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/14
 */
public class MDockerMetricsLog extends MMetricsBaseLog {
    public MDockerMetricsLog() {
        this.logType = MLogType.CONTAINER_METRICS_LOG;
    }
}
