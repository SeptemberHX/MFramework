package com.septemberhx.server.utils;

import com.septemberhx.common.bean.MBuildInfoRequest;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.MRequestUtils;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Yaml;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.util.UUID;


@Component
public class MServerUtils {

    private static String MClusterIpAddr;
    private static int MClusterPort;
    private static String buildCenterIpAddr;
    private static Integer buildCenterPort;

    @Value("${mserver.mcluster.ip}")
    public void setMClusterIpAddr(String MClusterIpAddr) {
        MServerUtils.MClusterIpAddr = MClusterIpAddr;
    }

    @Value("${mserver.mcluster.port}")
    public void setMClusterPort(Integer MClusterPort) {
        MServerUtils.MClusterPort = MClusterPort;
    }

    @Value("${mserver.buildcenter.ip}")
    public void setBuildCenterIpAddr(String buildCenterIpAddr) {
        MServerUtils.buildCenterIpAddr = buildCenterIpAddr;
    }

    @Value("${mserver.buildcenter.port}")
    public void setBuildCenterPort(Integer buildCenterPort) {
        MServerUtils.buildCenterPort = buildCenterPort;
    }

    private static void buildImage(String gitUrl, String branch, String projectName, String moduleName, String imageVersion) {

    }

    private static RestTemplate restTemplate = new RestTemplate();

    public static MInstanceInfoResponse fetchAllInstanceInfo() {
        return MRequestUtils.sendRequest(
                MUrlUtils.getMclusterFetchInstanceInfoUri(),
                null,
                MInstanceInfoResponse.class,
                RequestMethod.GET);
    }

    public static void notifyAddNewRemoteUri(String instanceId, String mObjectId, String funcName) {
        MInstanceRestInfoBean infoBean = new MInstanceRestInfoBean();
        infoBean.setRestAddress("");
        infoBean.setObjectId(mObjectId);
        infoBean.setFunctionName(funcName);
        MSetRestInfoRequest setRestInfoRequest = new MSetRestInfoRequest();
        setRestInfoRequest.setRestInfoBean(infoBean);
        setRestInfoRequest.setInstanceId(instanceId);
        MServerUtils.sendSetRestInfoRequest(setRestInfoRequest);
    }

    public static void notifyDeleteRemoteUri(String instanceId, String mObjectId, String funcName) {
        MInstanceRestInfoBean infoBean = new MInstanceRestInfoBean();
        infoBean.setRestAddress(null);
        infoBean.setObjectId(mObjectId);
        infoBean.setFunctionName(funcName);
        MSetRestInfoRequest setRestInfoRequest = new MSetRestInfoRequest();
        setRestInfoRequest.setRestInfoBean(infoBean);
        setRestInfoRequest.setInstanceId(instanceId);
        MServerUtils.sendSetRestInfoRequest(setRestInfoRequest);
    }

    private static void sendSetRestInfoRequest(MSetRestInfoRequest restInfoRequest) {
        MRequestUtils.sendRequest(
                MUrlUtils.getMClientAgentSetRestInfoUri(MClusterIpAddr, MClusterPort),
                restInfoRequest, null, RequestMethod.POST);
    }

    public static String getBuildUniqueId() {
        return "Build_" + UUID.randomUUID().toString();
    }

    public static V1Pod readPodYaml(String serviceName) {
        V1Pod pod = null;
        try {
            Object podYamlObj = Yaml.load(new File("./yaml/" + serviceName + ".yaml"));
            if (podYamlObj.getClass().getSimpleName().equals("V1Pod")) {
                pod = (V1Pod) podYamlObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pod;
    }

    public static void sendBuildInfo(MBuildInfoRequest mBuildInfoRequest) {
        URI buildUri = MUrlUtils.getBuildCenterBuildUri(buildCenterIpAddr, buildCenterPort);
        MRequestUtils.sendRequest(buildUri, mBuildInfoRequest, null, RequestMethod.POST);
    }
}
