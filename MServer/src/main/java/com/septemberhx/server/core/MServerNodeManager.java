package com.septemberhx.server.core;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.base.model.MServerNode;

import java.util.*;


/**
 * We assume that all server nodes in one cluster can connect to each other
 */
public class MServerNodeManager extends MObjectManager<MServerNode> {

    private MutableValueGraph<String, MNodeConnectionInfo> serverNodeGraph;

    public MServerNodeManager() {
        this.serverNodeGraph = ValueGraphBuilder.directed().build();
    }

    public void addConnectionInfo(MNodeConnectionInfo info, String startNodeId, String endNodeId) {
        serverNodeGraph.putEdgeValue(startNodeId, endNodeId, info);
    }

    public void removeConnectionInfo(String startNodeId, String endNodeId) {
        this.serverNodeGraph.removeEdge(startNodeId, endNodeId);
    }

    public void addNode(MServerNode serverNode) {
        this.objectMap.put(serverNode.getId(), serverNode);
        this.serverNodeGraph.addNode(serverNode.getId());
    }

    public List<MServerNode> getAllConnectedNodesOrderedDecent(String serverNodeId) {
        List<EndpointPair<String>> edgeList = new ArrayList<>(this.serverNodeGraph.incidentEdges(serverNodeId));
        List<MServerNode> successorList = new ArrayList<>();
        for (EndpointPair<String> edge : edgeList) {
            successorList.add(this.objectMap.get(edge.nodeV()));
        }
        Collections.sort(successorList, (o1, o2) -> {
            Optional<MNodeConnectionInfo> cIOption1 = serverNodeGraph.edgeValue(serverNodeId, o1.getId());
            Optional<MNodeConnectionInfo> cIOption2 = serverNodeGraph.edgeValue(serverNodeId, o2.getId());
            if (cIOption1.isPresent() && cIOption2.isPresent()) {
                return cIOption1.get().compareTo(cIOption2.get());
            } else {
                if (cIOption1.isPresent()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return successorList;
    }
}
