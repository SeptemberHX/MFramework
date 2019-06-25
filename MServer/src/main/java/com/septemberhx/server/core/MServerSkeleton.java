package com.septemberhx.server.core;


import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.server.base.MServiceInstance;
import com.septemberhx.server.utils.MServerUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MServerSkeleton {
    private static volatile MServerSkeleton instance;
    private Map<String, Map<String, String>> remoteInstanceIdMap;
    private MSystemModel currModel;

    private MServerSkeleton() {
        this.remoteInstanceIdMap = new HashMap<>();
        this.currModel = new MSystemModel();
    }

    public static MServerSkeleton getInstance() {
        if (instance == null) {
            synchronized (MServerSkeleton.class) {
                if (instance == null) {
                    instance = new MServerSkeleton();
                }
            }
        }
        return instance;
    }

    public URI getRemoteUri(String mObjectId, String funcName) {
        URI uri = null;
        if (this.remoteInstanceIdMap.containsKey(mObjectId)
                && this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
            if (this.remoteInstanceIdMap.containsKey(mObjectId)
                    && this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
                Optional<MServiceInstance> instance = this.currModel.getInstanceById(
                        this.remoteInstanceIdMap.get(mObjectId).get(funcName)
                );
                if (instance.isPresent()) {
                    MServiceInstance inst = instance.get();
                    uri = MUrlUtils.getRemoteUri(inst.getIp(), inst.getPort(), funcName);
                }
            }
        }
        return uri;
    }

    public void setRemoteUri(String instanceId, String mObjectId, String funcName) {
        if (!this.remoteInstanceIdMap.containsKey(mObjectId)
                || !this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
            this.addNewRemoteUri(instanceId, mObjectId, funcName);
        } else if (instanceId == null) {
            this.deleteRemoteUri(mObjectId, funcName);
        } else {
            this.replaceRemoteUri(instanceId, mObjectId, funcName);
        }
    }

    private void addNewRemoteUri(String instanceId, String mObjectId, String funcName) {
        if (!this.remoteInstanceIdMap.containsKey(mObjectId)) {
            this.remoteInstanceIdMap.put(mObjectId, new HashMap<>());
        }

        this.remoteInstanceIdMap.get(mObjectId).put(funcName, instanceId);
        Optional<MServiceInstance> instanceOp = this.currModel.getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyAddNewRemoteUri(mServiceInstance.getIp(), mObjectId, funcName));
    }

    private void replaceRemoteUri(String instanceId, String mObjectId, String funcName) {
        this.remoteInstanceIdMap.get(mObjectId).put(funcName, instanceId);
    }

    private void deleteRemoteUri(String mObjectId, String funcName) {
        this.remoteInstanceIdMap.get(mObjectId).remove(funcName);
        Optional<MServiceInstance> instanceOp = this.currModel.getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyDeleteRemoteUri(mServiceInstance.getIp(), mObjectId, funcName));
    }
}
