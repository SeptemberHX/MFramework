package com.septemberhx.agent.utils;

import com.septemberhx.agent.middleware.MDockerManager;
import com.septemberhx.agent.middleware.MDockerManagerK8SImpl;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.RequestUtils;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Yaml;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.HashMap;


public class MClientUtils {

    private static MDockerManager dockerManager = new MDockerManagerK8SImpl();

    public static void sendRestInfo(URI uri, MInstanceRestInfoBean infoBean) {
        RequestUtils.sendRequest(uri, infoBean, Object.class, RequestMethod.POST);
    }

    public static void deleteInstanceById(String instanceId) {
        dockerManager.deleteInstanceById(instanceId);
    }

    public static V1Deployment buildDeployment(String serviceName, String serviceInstanceId, String nodeId) {
        String deploymentName = serviceName + "-" + serviceInstanceId;

        V1Pod pod = readPodYaml(serviceName);
        pod.getMetadata().getLabels().put("app", deploymentName);
        if (pod.getSpec().getNodeSelector() == null) {
            pod.getSpec().setNodeSelector(new HashMap<>());
        }
        pod.getSpec().getNodeSelector().put("node", nodeId);

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
}
