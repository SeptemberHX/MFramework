package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;
import io.kubernetes.client.models.V1Pod;

public interface MDockerManager {
    public MDockerInfoBean getDockerInfoByIpAddr(String ipAddr);
    public boolean deleteInstanceById(String instanceId);
    public V1Pod deployInstanceOnNode(String nodeId, String instanceId, String serviceName, String imageUrl);
    public boolean checkIfDockerRunning(String ipAddr);
}
