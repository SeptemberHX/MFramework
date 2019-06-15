package com.septemberhx.server.base;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;


@Getter
@Setter
public class MServiceInstance extends MBaseObject {

    private Map<String, String> parentIdMap;
    private String nodeId;
    private String ip;

    public MServiceInstance(Map<String, String> parentIdMap, String nodeId, String ip) {
        this.parentIdMap = parentIdMap;
        this.nodeId = nodeId;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
