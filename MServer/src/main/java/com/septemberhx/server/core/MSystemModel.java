package com.septemberhx.server.core;


import com.septemberhx.common.bean.InstanceInfoBean;
import com.septemberhx.server.base.MServiceInstance;

public class MSystemModel {

    private MServiceInstanceManager mSIManager;
    private MServerNodeManager mSNManager;

    public MSystemModel() {
        this.mSIManager = new MServiceInstanceManager();
        this.mSNManager = new MServerNodeManager();
    }

    public void loadInstanceInfo(InstanceInfoBean instanceInfo) {
        this.mSIManager.add(new MServiceInstance(
                instanceInfo.getParentIdMap(),
                instanceInfo.getIp(),  // should query k8s to get the node id
                instanceInfo.getIp()
        ));
    }
}
