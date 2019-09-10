package com.septemberhx.agent.middleware;

import com.netflix.appinfo.InstanceInfo;
import com.septemberhx.common.bean.MInstanceInfoBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MServiceManager {
    Set<String> getNodeIdSet();
    Optional<String> getInstanceIdByIp(String ip);
    Optional<String> getNodeIdOfInstance(String instanceId);
    List<InstanceInfo> getInstanceInfoList();
    InstanceInfo getInstanceInfoById(String instanceId);
    public InstanceInfo getInstanceInfoByIpAndPort(String ipAddr);
}
