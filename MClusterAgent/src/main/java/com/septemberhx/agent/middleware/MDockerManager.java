package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;

public interface MDockerManager {
    public MDockerInfoBean getDockerInfoByIpAddr(String ipAddr);
    public void deleteInstanceById(String instanceId);
    public void deployInstanceOnNode(String serviceName, String serviceInstanceId, String nodeId);
    public boolean checkIfDockerRunning(String ipAddr);
}
