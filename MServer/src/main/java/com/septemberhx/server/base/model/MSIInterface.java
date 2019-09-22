package com.septemberhx.server.base.model;

import lombok.Getter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/22
 *
 * Interface of a service instance
 */
@Getter
public class MSIInterface {

    private String instanceId;      // the instance id
    private String interfaceName;   // the interface name
    private String objectId;        // the id of object which has the interface

    public MSIInterface(String instanceId, String objectId, String interfaceName) {
        this.instanceId = instanceId;
        this.objectId = objectId;
        this.interfaceName = interfaceName;
    }
}
