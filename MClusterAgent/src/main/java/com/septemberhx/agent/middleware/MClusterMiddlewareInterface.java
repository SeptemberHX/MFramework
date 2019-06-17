package com.septemberhx.agent.middleware;

import java.util.Optional;
import java.util.Set;

public interface MClusterMiddlewareInterface {
    Set<String> getNodeIdSet();
    Optional<String> getInstanceIdByIp(String ip);
    Optional<String> getNodeIdOfInstance(String instanceId);
}
