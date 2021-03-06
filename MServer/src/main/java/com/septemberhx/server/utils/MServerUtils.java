package com.septemberhx.server.utils;

import com.septemberhx.common.base.MUpdateCacheBean;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.ServerNodeType;
import com.septemberhx.common.bean.*;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.job.MDeleteJob;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;


@Component
public class MServerUtils {

    private static String mClusterIpAddr;
    private static Integer mClusterPort;
    private static String buildCenterIpAddr;
    private static Integer buildCenterPort;
    private static String deployConfigDir;
    private static String cloudAgentIpAddr;
    private static Integer cloudAgentPort;
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

    @Value("${mserver.cloudagent.ip}")
    public void setCloudAgentIpAddr(String cloudAgentIpAddr) {
        MServerUtils.cloudAgentIpAddr = cloudAgentIpAddr;
    }

    @Value("${mserver.cloudagent.port}")
    public void setCloudAgentPort(Integer cloudAgentPort) {
        MServerUtils.cloudAgentPort = cloudAgentPort;
    }

    public static MInstanceInfoResponse fetchAllInstanceInfo() {
        return MRequestUtils.sendRequest(
                MUrlUtils.getMclusterFetchInstanceInfoUri(mClusterIpAddr, mClusterPort),
                null,
                MInstanceInfoResponse.class,
                RequestMethod.GET);
    }

    public static List<String> fetchClusterLogsByDatetime(DateTime startTime, DateTime endTime) {
        MFetchLogsBetweenTimeRequest request = new MFetchLogsBetweenTimeRequest();
        request.setStartTime(startTime.getMillis());
        request.setEndTime(endTime.getMillis());
        return MRequestUtils.sendRequest(
                MUrlUtils.getMClusterAgentFetchLogsByTime(mClusterIpAddr, mClusterPort),
                request,
                MFetchLogsResponse.class,
                RequestMethod.POST
        ).getLogList();
    }

    public static List<MUser> fetchClusterUsers() {
        MAllUserBean userBean = MRequestUtils.sendRequest(
                MUrlUtils.getMClusterAllUserUrl(mClusterIpAddr, mClusterPort), null, MAllUserBean.class, RequestMethod.POST
        );

        if (userBean != null) {
            return userBean.getAllUserList();
        } else {
            return new ArrayList<>();
        }
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

    public static V1Pod getCompositionYaml(String serviceName) {
        V1Pod pod = readPodYaml("composition");
        pod.getMetadata().setGenerateName(serviceName + '-');
        pod.getSpec().getContainers().get(0).setName(serviceName);
        pod.getMetadata().getLabels().put("app", serviceName);
        return pod;
    }

    public static void sendBuildInfo(MBuildInfoRequest mBuildInfoRequest) {
        URI buildUri = MUrlUtils.getBuildCenterBuildUri(buildCenterIpAddr, buildCenterPort);
        MRequestUtils.sendRequest(buildUri, mBuildInfoRequest, null, RequestMethod.POST);
        logger.info(mBuildInfoRequest);
    }

    public static void sendCBuildInfo(MCompositionRequest mCompositionRequest) {
        URI cBuildUri = MUrlUtils.getBuildCenterCBuildUri(buildCenterIpAddr, buildCenterPort);
        MRequestUtils.sendRequest(cBuildUri, mCompositionRequest, null, RequestMethod.POST);
        logger.info(mCompositionRequest);
    }

    public static void sendDeployInfo(MDeployPodRequest mDeployPodRequest, ServerNodeType nodeType) {
        URI deployUri = null;
        if (nodeType == ServerNodeType.EDGE) {
            deployUri = MUrlUtils.getMClientAgentDeployUri(mClusterIpAddr, mClusterPort);
        } else {
            deployUri = MUrlUtils.getMClientAgentDeployUri(cloudAgentIpAddr, cloudAgentPort);
        }
        MRequestUtils.sendRequest(deployUri, mDeployPodRequest, null, RequestMethod.POST);
    }

    public static void sendSetApiCSInfo(MS2CSetApiCStatus ms2CSetApiCStatus) {
        URI setUri = MUrlUtils.getMClusterAgentSetApiCStatus(mClusterIpAddr, mClusterPort);
        MRequestUtils.sendRequest(setUri, ms2CSetApiCStatus, null, RequestMethod.POST);
        logger.info(ms2CSetApiCStatus);
    }


    /**
     * We only need to tell the cluster which instance should be deleted.
     * So we have to get the really instanceId that generated by the cluster which is started with the instanceid we used.
     * @param deleteJob
     */
    public static void sendDeleteInfo(MDeleteJob deleteJob, ServerNodeType nodeType) {
        String ourInstanceId = deleteJob.getInstanceId();
        Optional<MServiceInstance> infoBean = MServerSkeleton.getInstance().getInstanceInfo(ourInstanceId);
        if (infoBean.isPresent()) {
            MServiceInstance info = infoBean.get();
            Map<String, String> paras = new HashMap<>();
            paras.put("dockerInstanceId", info.getPodId());
            URI deleteUri;
            if (nodeType == ServerNodeType.EDGE) {
                deleteUri = MUrlUtils.getMClusterAgentDeleteInstanceUri(mClusterIpAddr, mClusterPort);
            } else {
                deleteUri = MUrlUtils.getMClusterAgentDeleteInstanceUri(cloudAgentIpAddr, cloudAgentPort);
            }
            MRequestUtils.sendRequest(deleteUri, paras, null, RequestMethod.GET);
            logger.info("Delete instance " + deleteJob.getInstanceId());
        }
    }

    public static void sendUpdateCache(MUpdateCacheBean updateCacheBean) {
        URI updateGatewaysUri = MUrlUtils.getMClusterUpdateGateways(mClusterIpAddr, mClusterPort);
        MRequestUtils.sendRequest(updateGatewaysUri, updateCacheBean, null, RequestMethod.POST);
    }
}
