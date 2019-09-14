package com.septemberhx.info.utils;

import com.septemberhx.info.beans.node.MNodeMetrics;
import com.septemberhx.info.beans.node.MNodesMetricsResponse;
import com.septemberhx.info.beans.pod.MPodMetrics;
import com.septemberhx.info.beans.pod.MPodsMetricsResponse;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

import static com.septemberhx.common.base.MClusterConfig.K8S_NAMESPACE;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/14
 */
@Component
public class K8sMetricUtils {

    public static String K8S_CLIENT_IP;
    public static Integer K8S_CLIENT_PORT;
    private static String PREFIX_METRICS_API = "/apis/metrics.k8s.io/v1beta1";
    private static String SUFFIX_METRICS_NODES = "/nodes";
    private static String SUFFIX_METRICS_PODS = "/pods";

    private static String getMetricsNodesUrl() {
        return PREFIX_METRICS_API + SUFFIX_METRICS_NODES;
    }

    private static String getMetricsPodsUrl() {
        return PREFIX_METRICS_API + SUFFIX_METRICS_PODS;
    }

    public static List<MNodeMetrics> getNodesMetrices() {
        List<MNodeMetrics> resultList = new ArrayList<>();
        MNodesMetricsResponse response = MRequestUtils.sendRequest(
                MUrlUtils.getRemoteUri(K8S_CLIENT_IP, K8S_CLIENT_PORT, getMetricsNodesUrl()),
                null,
                MNodesMetricsResponse.class,
                RequestMethod.GET
        );
        if (response != null) {
            resultList = response.getItems();
        }
        return resultList;
    }

    public static List<MPodMetrics> getPodsMetrics() {
        List<MPodMetrics> resultList = new ArrayList<>();
        MPodsMetricsResponse response = MRequestUtils.sendRequest(
                MUrlUtils.getRemoteUri(K8S_CLIENT_IP, K8S_CLIENT_PORT, getMetricsPodsUrl()),
                null,
                MPodsMetricsResponse.class,
                RequestMethod.GET
        );
        if (response != null) {
            resultList = response.getItems();
        }
        resultList.removeIf(podMetrics -> !podMetrics.getMetadata().getNamespace().equals(K8S_NAMESPACE));
        return resultList;
    }
}
