package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

public class MDockerManagerK8SImpl implements MDockerManager {

    private ApiClient client;
    private CoreV1Api coreV1Api;
    private ExtensionsV1beta1Api extensionsV1beta1Api;
    private static final String K8S_NAMESPACE = "kube-test";

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
                    System.out.println(item.getStatus().getPhase());
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
            Configuration.setDefaultApiClient(client);
            this.coreV1Api = new CoreV1Api();
            this.extensionsV1beta1Api = new ExtensionsV1beta1Api();
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
    public void deployInstanceOnNode(String serviceName, String serviceInstanceId, String nodeId) {

    }
}
