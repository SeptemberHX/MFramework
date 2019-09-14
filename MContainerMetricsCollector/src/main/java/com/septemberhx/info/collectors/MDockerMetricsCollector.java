package com.septemberhx.info.collectors;

import com.septemberhx.common.base.IInfoCollector;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MDockerMetricsLog;
import com.septemberhx.common.log.MNodeMetricsLog;
import com.septemberhx.common.utils.LogstashUtils;
import com.septemberhx.info.beans.node.MNodeMetrics;
import com.septemberhx.info.beans.pod.MPodMetrics;
import com.septemberhx.info.beans.pod.Usage;
import com.septemberhx.info.utils.K8sMetricUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/14
 */
public class MDockerMetricsCollector implements IInfoCollector {

    private String logstashIp;
    private Integer logstashPort;

    private static Long INTERVAL = TimeUnit.SECONDS.toMillis(60);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static Logger logger = LogManager.getLogger(MDockerMetricsCollector.class);


    @Override
    public void start() {
        this.initParams();
        if (K8sMetricUtils.K8S_CLIENT_PORT == null || K8sMetricUtils.K8S_CLIENT_IP == null) {
            logger.warn("Failed to start MetricsCollector due to the null value of cAdvisor ip/port");
            return;
        }

        if (this.logstashPort == null || this.logstashIp == null) {
            logger.warn("Failed to start MetricsCollector due to the null value of logstash ip/port");
            return;
        }

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fetchAndProcessMetrics();
            }
        }, 0, INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void fetchAndProcessMetrics() {
        List<MPodMetrics> podMetricsList = K8sMetricUtils.getPodsMetrics();
        List<MNodeMetrics> nodeMetricsList = K8sMetricUtils.getNodesMetrices();

        this.dealWithDockerMetrics(podMetricsList);
        this.dealWithNodeMetrics(nodeMetricsList);
    }

    private void dealWithDockerMetrics(List<MPodMetrics> podMetricsList) {
        List<MDockerMetricsLog> dockerMetricsLogList = new ArrayList<>();
        for (MPodMetrics podMetrics : podMetricsList) {
            MDockerMetricsLog dockerMetricsLog = new MDockerMetricsLog();
            dockerMetricsLog.setLogDateTime(DateTime.now());
            dockerMetricsLog.setLogHostname(podMetrics.getMetadata().getName());
            Usage resourceUsage = podMetrics.getContainers().get(0).getUsage();
            Long cpuUsage = Long.decode(resourceUsage.getCpu().substring(0, resourceUsage.getCpu().length() - 1));
            Long ramUsage = Long.decode(resourceUsage.getMemory().substring(0, resourceUsage.getMemory().length() - 2));
            dockerMetricsLog.setLogCpuUsage(cpuUsage);
            dockerMetricsLog.setLogRamUsage(ramUsage);
            dockerMetricsLogList.add(dockerMetricsLog);
            LogstashUtils.sendInfoToLogstash(this.logstashIp, this.logstashPort, MBaseLog.convertLog2JsonObejct(dockerMetricsLog).toString());
        }
    }

    private void dealWithNodeMetrics(List<MNodeMetrics> nodeMetricsList) {
        List<MNodeMetricsLog> nodeMetricsLogList = new ArrayList<>();
        for (MNodeMetrics podMetrics : nodeMetricsList) {
            MNodeMetricsLog nodeMetricsLog = new MNodeMetricsLog();
            nodeMetricsLog.setLogDateTime(DateTime.now());
            nodeMetricsLog.setLogHostname(podMetrics.getMetadata().getName());
            com.septemberhx.info.beans.node.Usage resourceUsage = podMetrics.getUsage();
            Long cpuUsage = Long.decode(resourceUsage.getCpu().substring(0, resourceUsage.getCpu().length() - 1));
            Long ramUsage = Long.decode(resourceUsage.getMemory().substring(0, resourceUsage.getMemory().length() - 2));
            nodeMetricsLog.setLogCpuUsage(cpuUsage);
            nodeMetricsLog.setLogRamUsage(ramUsage);
            nodeMetricsLogList.add(nodeMetricsLog);
            LogstashUtils.sendInfoToLogstash(this.logstashIp, this.logstashPort, MBaseLog.convertLog2JsonObejct(nodeMetricsLog).toString());
        }
    }

    @Override
    public void initParams() {
        K8sMetricUtils.K8S_CLIENT_IP = System.getenv("K8S_CLIENT_IP");
        logger.info("Set K8S_CLIENT_IP = " + K8sMetricUtils.K8S_CLIENT_IP);

        if (System.getenv().containsKey("K8S_CLIENT_PORT")) {
            try {
                K8sMetricUtils.K8S_CLIENT_PORT = Integer.valueOf(System.getenv("K8S_CLIENT_PORT"));
            } catch (Exception e) {
                ;
            }
        }
        logger.info("Set K8S_CLIENT_PORT = " + K8sMetricUtils.K8S_CLIENT_PORT);

        this.logstashIp = System.getenv("LOGSTASH_IP");
        logger.info("Set LOGSTASH_IP = " + this.logstashIp);

        if (System.getenv().containsKey("LOGSTASH_PORT")) {
            try {
                this.logstashPort = Integer.valueOf(System.getenv("LOGSTASH_PORT"));
            } catch (Exception e) {
                ;
            }
        }
        logger.info("Set LOGSTASH_PORT = " + this.logstashPort);

        K8sMetricUtils.K8S_CLIENT_IP = "192.168.1.102";
        K8sMetricUtils.K8S_CLIENT_PORT = 8082;

        this.logstashIp = "192.168.1.102";
        this.logstashPort = 32001;
    }
}
