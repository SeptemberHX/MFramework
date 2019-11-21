package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;
import io.kubernetes.client.models.V1Pod;

public interface MDockerManager {
    public MDockerInfoBean getDockerInfoByIpAddr(String ipAddr);
    public void deleteInstanceById(String instanceId);
    public V1Pod deployInstanceOnNode(String nodeId, String instanceId, V1Pod pod);
    public boolean checkIfDockerRunning(String ipAddr);
}
