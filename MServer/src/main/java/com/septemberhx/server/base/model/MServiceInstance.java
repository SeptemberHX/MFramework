package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;
import java.util.Set;


@Getter
@Setter
public class MServiceInstance extends MBaseObject {

    private Map<String, String> parentIdMap;
    private String nodeId;
    private String ip;
    private Integer port;
    private Set<String> mObjectIdSet;
    private String serviceName;
    private String serviceId;
    private String podId;

    public MServiceInstance(Map<String, String> parentIdMap, String nodeId, String ip, Integer port, String instanceId, Set<String> mObjectIdSet, String serviceName, String serviceId, String podId) {
        this.parentIdMap = parentIdMap;
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
        this.id = instanceId;
        this.mObjectIdSet = mObjectIdSet;
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.podId = podId;
    }

    public MServiceInstance deepClone() {
        return new MServiceInstance(
                this.parentIdMap,
                this.nodeId,
                this.ip,
                this.port,
                this.id,
                this.mObjectIdSet,
                this.serviceName,
                this.serviceId,
                this.podId
        );
    }

    @Override
    public String toString() {
        return "MServiceInstance{" +
                "parentIdMap=" + parentIdMap +
                ", nodeId='" + nodeId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", mObjectIdSet=" + mObjectIdSet +
                ", serviceName='" + serviceName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", podId='" + podId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
