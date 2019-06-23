package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

public class MDockerManagerK8SImpl implements MDockerManager {

    private ApiClient client;
    private CoreV1Api coreV1Api;

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
            V1PodList list = this.coreV1Api.listNamespacedPod("kube-test", null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                if (item.getStatus().getPodIP().equals(ipAddr)) {
                    infoBean.setHostIp(item.getStatus().getHostIP());
                    infoBean.setInstanceId(item.getMetadata().getName());
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return infoBean;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
