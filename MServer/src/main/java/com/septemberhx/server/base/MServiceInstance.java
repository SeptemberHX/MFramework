package com.septemberhx.server.base;

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
    private int port;
    private Set<String> mObjectIdSet;

    public MServiceInstance(Map<String, String> parentIdMap, String nodeId, String ip, int port, String instanceId, Set<String> mObjectIdSet) {
        this.parentIdMap = parentIdMap;
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
        this.id = instanceId;
        this.mObjectIdSet = mObjectIdSet;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
