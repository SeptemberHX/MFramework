package com.septemberhx.server.utils;

import com.septemberhx.common.bean.*;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.MRequestUtils;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.nio.file.Paths;


@Component
public class MServerUtils {

    private static String mClusterIpAddr;
    private static Integer mClusterPort;
    private static String buildCenterIpAddr;
    private static Integer buildCenterPort;
    private static String deployConfigDir;
    private static Logger logger = LogManager.getLogger(MServerUtils.class);

    @Value("${mserver.mcluster.ip}")
    public void setMClusterIpAddr(String MClusterIpAddr) {
        MServerUtils.mClusterIpAddr = MClusterIpAddr;
    }

    @Value("${mserver.mcluster.port}")
    public void setMClusterPort(Integer MClusterPort) {
        MServerUtils.mClusterPort = MClusterPort;
    }

    @Value("${mserver.buildcenter.ip}")
    public void setBuildCenterIpAddr(String buildCenterIpAddr) {
        MServerUtils.buildCenterIpAddr = buildCenterIpAddr;
    }

    @Value("${mserver.buildcenter.port}")
    public void setBuildCenterPort(Integer buildCenterPort) {
        MServerUtils.buildCenterPort = buildCenterPort;
    }

    @Value("${mserver.deploy.dir}")
    public void setDeployConfigDir(String deployConfigDir) {
        MServerUtils.deployConfigDir = deployConfigDir;
    }

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
                MUrlUtils.getMClientAgentSetRestInfoUri(mClusterIpAddr, mClusterPort),
                restInfoRequest, null, RequestMethod.POST);
    }

    public static V1Pod readPodYaml(String serviceName) {
        V1Pod pod = null;
        try {
            pod = Yaml.loadAs(Paths.get(deployConfigDir, serviceName + ".yaml").toFile(), V1Pod.class);
        } catch (Exception e) {
            logger.warn("Exception happened when read pod yaml");
            logger.warn(e);
        }
        return pod;
    }

    public static void sendBuildInfo(MBuildInfoRequest mBuildInfoRequest) {
        URI buildUri = MUrlUtils.getBuildCenterBuildUri(buildCenterIpAddr, buildCenterPort);
        MRequestUtils.sendRequest(buildUri, mBuildInfoRequest, null, RequestMethod.POST);
        logger.info(mBuildInfoRequest);
    }

    public static void sendDeployInfo(MDeployPodRequest mDeployPodRequest) {
        URI deployUri = MUrlUtils.getMClientAgentDeployUri(mClusterIpAddr, mClusterPort);
        MRequestUtils.sendRequest(deployUri, mDeployPodRequest, null, RequestMethod.POST);
        logger.info(mDeployPodRequest);
    }

    public static void sendSetApiCSInfo(MS2CSetApiCStatus ms2CSetApiCStatus) {
        URI setUri = MUrlUtils.getMClusterAgentSetApiCStatus(mClusterIpAddr, mClusterPort);
        MRequestUtils.sendRequest(setUri, ms2CSetApiCStatus, null, RequestMethod.POST);
        logger.info(ms2CSetApiCStatus);
    }
}
