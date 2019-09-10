package com.septemberhx.agent.middleware;

import com.google.gson.reflect.TypeToken;
import com.septemberhx.agent.utils.MClientUtils;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.septemberhx.common.base.MClusterConfig.K8S_NAMESPACE;

public class MWatchPodStatusThread extends Thread {

    private static Logger logger = LogManager.getLogger(MWatchPodStatusThread.class);
    private ApiClient client;
    private CoreV1Api coreV1Api;

    public MWatchPodStatusThread(String k8sClientUrl) {
        try {
            if (k8sClientUrl == null) {
                this.client = Config.defaultClient();
            } else {
                this.client = Config.fromUrl(k8sClientUrl);
            }
            this.client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);
            Configuration.setDefaultApiClient(client);
            this.coreV1Api = new CoreV1Api(this.client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
//        while (true) {
        try {
            Watch<V1Pod> watch = null;
            Map<String, String> lastPodStatusMap = new HashMap<>();
            try {
                watch = Watch.createWatch(
                        client,
                        coreV1Api.listNamespacedPodCall(K8S_NAMESPACE, null, null, null, null, null, null, null, Integer.MAX_VALUE, Boolean.TRUE, null, null),
                        new TypeToken<Watch.Response<V1Pod>>() {
                        }.getType()
                );

                for (Watch.Response<V1Pod> item : watch) {
                    String podStatus = item.object.getStatus().getPhase();
                    String podName = item.object.getMetadata().getName();
                    logger.debug(item.type + "|" + podName + "|" + podStatus);
                    switch (item.type) {
                        case "DELETED":
                            logger.info(podName + " now deleted");
                            break;
                        case "MODIFIED":
                            if ("Running".equals(podStatus) && !lastPodStatusMap.get(podName).equals(podStatus)) {
                                logger.info(podName + " now created");
                                MClientUtils.dealWithNewPodRunning(item.object);
                            }
                            break;
                        case "ADDED":
                            lastPodStatusMap.put(podName, podStatus);
                            break;
                        default:
                            break;
                    }
                }
            } catch (ApiException e) {
                e.printStackTrace();
            } finally {
                if (watch != null) {
                    watch.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }
}
