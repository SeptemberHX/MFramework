package com.septemberhx.agent.middleware;

import com.septemberhx.agent.utils.MClientUtils;
import com.septemberhx.common.bean.MInstanceInfoBean;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.*;

public class MK8SMiddleware implements MClusterMiddlewareInterface {

    private ApiClient client;
    private CoreV1Api coreV1Api;

    public MK8SMiddleware(String k8sClientUrl) {
        this.initConnection(k8sClientUrl);
    }

    public MK8SMiddleware() {
        this.initConnection(null);
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

    @Override
    public Set<String> getNodeIdSet() {
        Set<String> nodeIdSet = new HashSet<>();
        try {
            V1NodeList nodeList = this.coreV1Api.listNode(false, null, null, null, null, null, null, null, null);
            for (V1Node item : nodeList.getItems()) {
                nodeIdSet.add(item.getMetadata().getName());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return nodeIdSet;
    }

    @Override
    public Optional<String> getInstanceIdByIp(String ip) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNodeIdOfInstance(String instanceId) {
        return Optional.empty();
    }

    /**
     * Get all instances in the system
     * Todo: Select all pods with selector
     * @return
     */
    @Override
    public List<MInstanceInfoBean> getInstanceInfoList() {
        List<MInstanceInfoBean> infoBeanList = new LinkedList<>();
        try {
            V1PodList list = this.coreV1Api.listNamespacedPod("kube-test", null, null, null, null, null, null, null, null, null);
            for (V1Pod item : list.getItems()) {
                MInstanceInfoBean instanceInfoBean = new MInstanceInfoBean();
                instanceInfoBean.setId(item.getMetadata().getUid());
                instanceInfoBean.setIp(item.getStatus().getPodIP());
                instanceInfoBean.setNodeId(item.getStatus().getHostIP());
                instanceInfoBean.setParentIdMap(MClientUtils.getParentIdMap(instanceInfoBean.getIp()));
                instanceInfoBean.setApiMap(MClientUtils.getApiMap(instanceInfoBean.getIp()));
                infoBeanList.add(instanceInfoBean);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return infoBeanList;
    }
}
