package com.septemberhx.info.collectors.MetricsCollector;

import com.septemberhx.info.utils.CAdvisorUtils;
import com.septemberhx.common.base.IInfoCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 *
 * This class is used to get the metrics of the machine and the containers.
 *
 * Consideration that the K8S has it's own metrics api,
 *  we will get metrics of the containers in the MClusterAgent instead
 */
public class MetricsCollector implements IInfoCollector {

    private static String cAdvisorIpAddr;
    private static Integer cAdvisorPort;
    private static long INTERVAL = TimeUnit.SECONDS.toMillis(5);

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static Logger logger = LogManager.getLogger(MetricsCollector.class);

    @Override
    public void start() {
        this.initParams();
        if (cAdvisorPort == null || cAdvisorIpAddr == null) {
            logger.warn("Failed to start MetricsCollector due to the null value of cAdvisor ip/port");
            return;
        }

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                dealWithMetrics(CAdvisorUtils.getMachineState(cAdvisorIpAddr, cAdvisorPort));
            }
        }, 0, INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void dealWithMetrics(JSONObject object) {
        // todo: reconstruct
//        System.out.println(object);
    }

    @Override
    public void initParams() {
        MetricsCollector.cAdvisorIpAddr = System.getenv("MCLIENT_CADVISOR_IP");
        logger.info("Set cAdvisorIpAddr = " + MetricsCollector.cAdvisorIpAddr);

        if (System.getenv().containsKey("MCLIENT_CADVISOR_PORT")) {
            try {
                MetricsCollector.cAdvisorPort = Integer.valueOf(System.getenv("MCLIENT_CADVISOR_PORT"));
            } catch (Exception e) {
                ;
            }
        }
        logger.info("Set cAdvisorPort = " + MetricsCollector.cAdvisorPort);

//        MetricsCollector.cAdvisorIpAddr = "192.168.1.102";
//        MetricsCollector.cAdvisorPort = 4042;
    }

    public static void main(String[] args) {
        MetricsCollector metricsCollector = new MetricsCollector();
        metricsCollector.start();
    }
}
