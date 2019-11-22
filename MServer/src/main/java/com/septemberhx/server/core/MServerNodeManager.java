package com.septemberhx.server.core;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.common.base.MPosition;
import com.septemberhx.server.base.model.MServerNode;

import java.util.*;
import java.util.stream.Collectors;


/**
 * We assume that all server nodes in one cluster can connect to each other
 */
public class MServerNodeManager extends MObjectManager<MServerNode> {

    private MutableValueGraph<String, MNodeConnectionInfo> serverNodeGraph;

    public MServerNodeManager() {
        this.serverNodeGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
    }

    public void addConnectionInfo(MNodeConnectionInfo info, String startNodeId, String endNodeId) {
        serverNodeGraph.putEdgeValue(startNodeId, endNodeId, info);
    }

    public void removeConnectionInfo(String startNodeId, String endNodeId) {
        this.serverNodeGraph.removeEdge(startNodeId, endNodeId);
    }

    public void add(MServerNode serverNode) {
        this.objectMap.put(serverNode.getId(), serverNode);
        this.serverNodeGraph.addNode(serverNode.getId());
    }

    /**
     * Get other nodes that the delay between it and given node is less than MAX_DELAY_TOLERANCE
     * The result will be ordered by the delay in decent
     * @param serverNodeId: given node id
     * @return server node list
     */
    public List<MServerNode> getConnectedNodesDecentWithDelayTolerance(String serverNodeId) {
        List<EndpointPair<String>> edgeList = new ArrayList<>(this.serverNodeGraph.incidentEdges(serverNodeId));
        List<MServerNode> successorList = new ArrayList<>();
        for (EndpointPair<String> edge : edgeList) {
            if (!edge.nodeV().equals(serverNodeId)) {
                successorList.add(this.objectMap.get(edge.nodeV()));
            }
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

        successorList = successorList.stream().filter(n -> {
                Optional<MNodeConnectionInfo> eInfo = serverNodeGraph.edgeValue(serverNodeId, n.getId());
                return eInfo.isPresent() && eInfo.get().getDelay() <= MAdaptiveSystem.MAX_DELAY_TOLERANCE;
            }).collect(Collectors.toList());
        return successorList;
    }

    public String getClosestNodeId(MPosition position) {
        if (this.objectMap.size() == 0) {
            return null;
        }
        List<String> nodeIdList = new ArrayList<>(this.objectMap.keySet());
        String targetNodeId = nodeIdList.get(0);
        Double distance = this.objectMap.get(targetNodeId).getPosition().distanceTo(position);

        for (String nodeId : nodeIdList) {
            Double d = this.objectMap.get(nodeId).getPosition().distanceTo(position);
            if (d < distance) {
                distance = d;
                targetNodeId = nodeId;
            }
        }
        return targetNodeId;
    }

    public List<MServerNode> getFixedOrderNodeList() {
        List<MServerNode> nodeList = this.getAllValues();
        nodeList.sort(new Comparator<MServerNode>() {
            @Override
            public int compare(MServerNode o1, MServerNode o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return nodeList;
    }

    public MNodeConnectionInfo getConnectionInfo(String fromNodeId, String toNodeId) {
        return this.serverNodeGraph.edgeValueOrDefault(fromNodeId, toNodeId, null);
    }

    public double getMaxBandwidth() {
        double r = 0;
        for (MServerNode node : this.getAllValues()) {
            if (node.getBandwidth() > r) {
                r = node.getBandwidth();
            }
        }

        for (EndpointPair<String> edge : this.serverNodeGraph.edges()) {
            if (this.serverNodeGraph.edgeValue(edge) != null && this.serverNodeGraph.edgeValue(edge).get().getBandwidth() > r) {
                r = this.serverNodeGraph.edgeValue(edge).get().getBandwidth();
            }
        }
        return r;
    }

    public double getMaxDelay() {
        double r = 0;
        for (MServerNode node : this.getAllValues()) {
            if (node.getDelay() > r) {
                r = node.getDelay();
            }
        }

        for (EndpointPair<String> edge : this.serverNodeGraph.edges()) {
            if (this.serverNodeGraph.edgeValue(edge) != null && this.serverNodeGraph.edgeValue(edge).get().getDelay() > r) {
                r = this.serverNodeGraph.edgeValue(edge).get().getDelay();
            }
        }
        return r;
    }

    public double getMinDelayBetweenNodeAndUser() {
        double r = -1;
        for (MServerNode node : this.getAllValues()) {
            if (r < 0 || node.getDelay() < r) {
                r = node.getDelay();
            }
        }
        return r;
    }
}
