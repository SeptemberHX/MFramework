package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MInstanceInfoBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MServiceManager {
    Set<String> getNodeIdSet();
    Optional<String> getInstanceIdByIp(String ip);
    Optional<String> getNodeIdOfInstance(String instanceId);
    List<MInstanceInfoBean> getInstanceInfoList();
}
