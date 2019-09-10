package com.septemberhx.agent.middleware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.septemberhx.agent.utils.MClientUtils;
import com.septemberhx.common.bean.MDeployPodRequest;
import com.septemberhx.common.bean.MDockerInfoBean;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.septemberhx.common.base.MClusterConfig.K8S_NAMESPACE;

public class MDockerManagerK8SImpl implements MDockerManager {

    private ApiClient client;
    private CoreV1Api coreV1Api;
    private ExtensionsV1beta1Api extensionsV1beta1Api;
    private static Logger logger = LogManager.getLogger(MDockerManagerK8SImpl.class);

    public MDockerManagerK8SImpl(String k8sClientUrl) {
        this.initConnection(k8sClientUrl);
    }

    public MDockerManagerK8SImpl() {
        this.initConnection(null);
    }

    @Override
    public MDockerInfoBean getDockerInfoByIpAddr(String ipAddr) {
        MDockerInfoBean infoBean = null;
        try {
            infoBean = new MDockerInfoBean();
            V1PodList list = this.coreV1Api.listNamespacedPod(K8S_NAMESPACE, null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                if (item.getStatus() != null &&
                        "Running".equals(item.getStatus().getPhase()) && ipAddr.equals(item.getStatus().getPodIP())) {
                    infoBean.setHostIp(item.getStatus().getHostIP());
                    infoBean.setInstanceId(item.getMetadata().getName());
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return infoBean;
    }

    @Override
    public boolean checkIfDockerRunning(String ipAddr) {
        try {
            V1PodList list = this.coreV1Api.listNamespacedPod(K8S_NAMESPACE, null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                if (item.getStatus().getPodIP().equals(ipAddr)) {
                    if (item.getStatus().getPhase().equals("Running")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initConnection(String k8sClientUrl) {
        try {
            if (k8sClientUrl == null) {
                this.client = Config.defaultClient();
            } else {
                this.client = Config.fromUrl(k8sClientUrl);
            }
            this.client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);
            Configuration.setDefaultApiClient(client);
            this.coreV1Api = new CoreV1Api(this.client);
            this.extensionsV1beta1Api = new ExtensionsV1beta1Api(this.client);

            Thread watchThread = new MWatchPodStatusThread(k8sClientUrl);
            watchThread.start();
//            watchPodStatus();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteInstanceById(String instanceId) {
        try {
            // We assume that the pods are deployed by Deployment, or we will have trouble in managing the dockers
            // Because we control how the pods are deployed, we can know the Deployment by the instanceId
            // For example: sample-service-fcb46ff9-k99wc is deployed by sample-service
            String deploymentName = instanceId.substring(
                    0, instanceId.substring(0, instanceId.lastIndexOf("-")).lastIndexOf("-")
            );
            this.extensionsV1beta1Api.deleteNamespacedDeployment(
                    deploymentName,
                    K8S_NAMESPACE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Foreground"
            );
        } catch (IllegalStateException e) {
            ;
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public V1Pod deployInstanceOnNode(String nodeId, V1Pod podBody) {
        // fill the node selector
        if (podBody.getSpec().getNodeSelector() == null) {
            podBody.getSpec().setNodeSelector(new HashMap<>());
        }
        podBody.getSpec().getNodeSelector().put("node", nodeId);

        V1Pod resultPod = null;
        try {
            resultPod = coreV1Api.createNamespacedPod(K8S_NAMESPACE, podBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return resultPod;
    }

    public static void main(String[] args) {
//        MDockerManagerK8SImpl dockerManager = new MDockerManagerK8SImpl("http://192.168.1.102:8082");
        V1Pod pod = MClientUtils.readPodYaml("sampleservice");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pod));
//        dockerManager.deployInstanceOnNode("ices-104", pod);
//        MDeployPodRequest podRequest = new MDeployPodRequest("test-1", "ices-104", pod);
//        MRequestUtils.sendRequest(MUrlUtils.getRemoteUri("192.168.1.102", 9000, "/magent/deploy"), podRequest, null, RequestMethod.POST);
    }
}
