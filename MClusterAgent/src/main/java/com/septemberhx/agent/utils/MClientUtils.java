package com.septemberhx.agent.utils;

import com.netflix.appinfo.InstanceInfo;
import com.septemberhx.agent.middleware.MDockerManager;
import com.septemberhx.agent.middleware.MDockerManagerK8SImpl;
import com.septemberhx.agent.middleware.MServiceManager;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.*;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class MClientUtils {

    @Autowired
    private MServiceManager clusterMiddleware;

    @Value("${mclientagent.server.ip}")
    private String serverIpAddr;

    @Value("${mclientagent.server.port}")
    private Integer serverPort;

    private static MDockerManager dockerManager = new MDockerManagerK8SImpl();
    private Map<String, MDeployPodRequest> podDuringDeploying = new HashMap<>();  // deployed but not running
    private static Logger logger = LogManager.getLogger(MClientUtils.class);

    public static void sendRestInfo(URI uri, MInstanceRestInfoBean infoBean) {
        MRequestUtils.sendRequest(uri, infoBean, Object.class, RequestMethod.POST);
    }

    public static void deleteInstanceById(String instanceId) {
        dockerManager.deleteInstanceById(instanceId);
    }

    public static V1Deployment buildDeployment(String serviceName, String serviceInstanceId, String nodeId, String image) {
        String deploymentName = serviceName + "-" + serviceInstanceId;

        // read pod configure file supplied by users
        V1Pod pod = readPodYaml(serviceName);
        // fill the container image
        pod.getMetadata().getLabels().put("app", deploymentName);
        for (V1Container container : pod.getSpec().getContainers()) {
            if (container.getName().equals(serviceName)) {
                container.setImage(image);
            }
        }
        // fill the node selector
        if (pod.getSpec().getNodeSelector() == null) {
            pod.getSpec().setNodeSelector(new HashMap<>());
        }
        pod.getSpec().getNodeSelector().put("node", nodeId);

        // build deployment configure file
        V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();
        podTemplateSpec.setSpec(pod.getSpec());
        podTemplateSpec.setMetadata(pod.getMetadata());
        V1Deployment deployment =
                new V1DeploymentBuilder(true)
                    .withApiVersion("extensions/v1beta1")
                    .withKind("Deployment")
                    .withNewMetadata()
                    .withNamespace(MClusterConfig.MCLUSTER_DOCKER_NAMESPACE)
                    .withName(deploymentName)
                    .endMetadata()
                    .withNewSpec()
                    .withReplicas(1)
                    .withTemplate(podTemplateSpec)
                    .endSpec()
                    .build();
        // save the configure file
        try {
            FileWriter writer = new FileWriter("./test.yaml");
            Yaml.dump(deployment, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deployment;
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

    public List<MInstanceInfoBean> getInstanceInfoList() {
        List<MInstanceInfoBean> result = new ArrayList<>();
        for (InstanceInfo info : this.clusterMiddleware.getInstanceInfoList()) {
            MInstanceInfoBean infoBean = this.transformInstance(info, 0);
            if (infoBean != null) {
                result.add(infoBean);
            }
        }
        return result;
    }

    public MInstanceInfoBean getInstanceInfoById(String instanceId) {
        InstanceInfo info = this.clusterMiddleware.getInstanceInfoById(instanceId);
        if (info == null) {
            return null;
        } else {
            return this.transformInstance(info, 0);
        }
    }

    public MInstanceInfoBean getInstanceInfoByIp(String ipAddr) {
        InstanceInfo baseInfo = this.clusterMiddleware.getInstanceInfoByIpAndPort(ipAddr);
        if (baseInfo ==null) {
            return null;
        } else {
            return this.transformInstance(baseInfo, 0);
        }
    }

    /**
     * collect necessary information so we can build a MInstanceInfoBean from InstanceInfo
     * @param instanceInfo
     * @return
     */
    public MInstanceInfoBean transformInstance(InstanceInfo instanceInfo, int backwardPort) {
        MInstanceInfoBean instanceInfoBean = new MInstanceInfoBean();

        if (!instanceInfo.getMetadata().containsKey(MClusterConfig.MCLUSTER_SERVICE_METADATA_NAME)
                || !instanceInfo.getMetadata().get(MClusterConfig.MCLUSTER_SERVICE_METADATA_NAME).equals(
                MClusterConfig.MCLUSTER_SERVICE_METADATA_VALUE)) {
            return null;
        }

        instanceInfoBean.setId(instanceInfo.getId());
        instanceInfoBean.setIp(instanceInfo.getIPAddr());

        int instancePort = instanceInfo.getPort();
        if (instancePort == 0) instancePort = backwardPort;

        instanceInfoBean.setPort(instancePort);

        MClientInfoBean response = MRequestUtils.sendRequest(
                MUrlUtils.getMClusterAgentFetchClientInfoUri(instanceInfo.getIPAddr(), instancePort),
                null,
                MClientInfoBean.class,
                RequestMethod.GET
        );

        if (response == null) {
            return instanceInfoBean;
        }

        instanceInfoBean.setParentIdMap(response.getParentIdMap());
        instanceInfoBean.setApiMap(response.getApiMap());
        instanceInfoBean.setMObjectIdMap(response.getMObjectIdSet());

        if (!dockerManager.checkIfDockerRunning(instanceInfo.getIPAddr())) {
            return instanceInfoBean;
        }
        instanceInfoBean.setDockerInfo(dockerManager.getDockerInfoByIpAddr(instanceInfo.getIPAddr()));
        return instanceInfoBean;
    }

    /**
     * When the pod is ready, this function will be called.
     * It will gather instance information from service registry and send it to server side.
     * @param pod
     */
    public static void dealWithNewPodRunning(V1Pod pod) {
//        if (podDuringDeploying.containsKey(pod.getMetadata().getName())) {
//            MDeployNotifyRequest deployNotifyRequest = new MDeployNotifyRequest(
//                    podDuringDeploying.get(pod.getMetadata().getName()).getId(),
//                    MDeployNotifyRequest.DeployStatus.SUCCESS
//            );
//            System.out.println(pod);
//            InstanceInfo info = instance.clusterMiddleware.getInstanceInfoByIpAndPort(pod.getStatus().getPodIP());
//            MInstanceInfoBean infoBean = instance.transformInstance(info);
//            System.out.println(infoBean);
//            System.out.println(deployNotifyRequest);
//        }
    }

    public void depoly(MDeployPodRequest mDeployPodRequest) {
        try {
            V1Pod pod = dockerManager.deployInstanceOnNode(mDeployPodRequest.getNodeId(), mDeployPodRequest.getPodBody());
            podDuringDeploying.put(pod.getMetadata().getName(), mDeployPodRequest);
            logger.info("Job " + mDeployPodRequest.getId() + " dispatched");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean notifyDeployJobFinished(MInstanceInfoBean infoBean) {
        if (infoBean.getDockerInfo() == null) return false;
        String instanceId = infoBean.getDockerInfo().getInstanceId();
        if (!this.podDuringDeploying.containsKey(instanceId)) return false;

        String jobId = podDuringDeploying.get(infoBean.getDockerInfo().getInstanceId()).getId();
        MDeployNotifyRequest deployNotifyRequest = new MDeployNotifyRequest(jobId, instanceId);
        MRequestUtils.sendRequest(MUrlUtils.getMServerDeployNotifyJobUri(serverIpAddr, serverPort), deployNotifyRequest, null, RequestMethod.POST);

        logger.info("Job " + jobId + " finished and notified");
        this.podDuringDeploying.remove(instanceId);
        return true;
    }
}
