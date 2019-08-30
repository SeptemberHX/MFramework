package com.septemberhx.infocollector.collector.MetricsCollector;

import com.septemberhx.infocollector.utils.CAdvisorUtils;
import com.septemberhx.infocollector.collector.IInfoCollector;
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

    private static String cAdvisorIpAddr = "192.168.1.102";
    private static Integer cAdvisorPort = 4042;
    private static long INTERVAL = TimeUnit.SECONDS.toMillis(5);

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public void start() {
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                dealWithMetrics(CAdvisorUtils.getMachineState(cAdvisorIpAddr, cAdvisorPort));
            }
        }, 0, INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void dealWithMetrics(JSONObject object) {
        // todo: reconstruct
        System.out.println(object);
    }

    public static void main(String[] args) {
        MetricsCollector metricsCollector = new MetricsCollector();
        metricsCollector.start();
    }
}
