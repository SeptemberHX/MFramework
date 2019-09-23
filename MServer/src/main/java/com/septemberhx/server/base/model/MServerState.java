package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 *
 * This class describes the real-time state of the server node.
 * It will change over time.
 * The id is the server node id.
 */
@Getter
@Setter
public class MServerState extends MBaseObject {
    private MResource resource;

    public MServerState(String nodeId) {
        this.id = nodeId;
    }
}
