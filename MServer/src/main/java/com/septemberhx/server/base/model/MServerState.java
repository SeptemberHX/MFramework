package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import com.septemberhx.common.base.MResource;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 *
 * This class describes the real-time state of the server node.
 * It will change over time and will be only changed by the monitors.
 * The id is the server node id.
 */
@Getter
@Setter
public class MServerState extends MBaseObject {
    private MResource resource;

    public MServerState(String nodeId) {
        this.id = nodeId;
    }

    public MServerState mclone() {
        MServerState serverState = new MServerState(this.id);
        serverState.resource = new MResource(this.resource);
        return  serverState;
    }
}
