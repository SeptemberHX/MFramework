package com.septemberhx.server.core;


import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.MServerNode;

import java.util.HashMap;
import java.util.Map;

public class MServerNodeManager extends MObjectManager<MServerNode> {

    private Map<String, Map<String, MNodeConnectionInfo>> connectionInfoMap;

    public MServerNodeManager() {
        this.connectionInfoMap = new HashMap<>();
    }

    public void addConnectionInfo(MNodeConnectionInfo info, String startNodeId, String endNodeId) {
        if (!connectionInfoMap.containsKey(startNodeId)) {
            connectionInfoMap.put(startNodeId, new HashMap<>());
        }

        connectionInfoMap.get(startNodeId).put(endNodeId, info);
    }

    public void removeConnectionInfo(String startNodeId, String endNodeId) {
        this.connectionInfoMap.getOrDefault(startNodeId, new HashMap<>()).remove(endNodeId);
    }
}
