package com.septemberhx.server.core;


import com.septemberhx.common.bean.MGetRemoteUriRequest;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.server.base.MServiceInstance;
import com.septemberhx.server.utils.MServerUtils;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MServerSkeleton {

    private static Logger logger = LogManager.getLogger(MServerSkeleton.class);
    private static volatile MServerSkeleton instance;
    private Map<String, Map<String, String>> remoteInstanceIdMap;
    private MSystemModel currModel;

    @Getter
    private MJobManager jobManager = new MJobManager();

    @Getter
    private MRepoManager repoManager;

    private MServerSkeleton() {
        this.remoteInstanceIdMap = new HashMap<>();
        this.currModel = new MSystemModel();
        this.repoManager = MRepoManager.loadFromFile("./project.json");
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

    public void updateInstanceInfo(MInstanceInfoBean infoBean) {
        this.currModel.loadInstanceInfo(infoBean);
    }

    public Optional<MServiceInstance> getInstanceInfo(String instanceId) {
        return this.currModel.getInstanceById(instanceId);
    }

    public List<MServiceInstance> getAllInstanceInfos() {
        return this.currModel.getAllServiceInstance();
    }

    // Remote Uri stuffs below ---------------------------------

    public URI getRemoteUri(MGetRemoteUriRequest remoteUriRequest) {
        logger.debug(remoteUriRequest);
        String mObjectId = remoteUriRequest.getObjectId();
        String funcName = remoteUriRequest.getFunctionName();
        URI uri = null;

        if (this.remoteInstanceIdMap.containsKey(mObjectId)
                && this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
            Optional<MServiceInstance> instance = this.currModel.getInstanceById(
                    this.remoteInstanceIdMap.get(mObjectId).get(funcName)
            );
            if (instance.isPresent()) {
                MServiceInstance inst = instance.get();
                String rawPath = remoteUriRequest.getRawPatterns();
                if (rawPath.startsWith("[") && rawPath.endsWith("]")) {
                    rawPath = rawPath.substring(1, rawPath.lastIndexOf("]"));
                }
                uri = MUrlUtils.getRemoteUri(inst.getIp(), inst.getPort(), rawPath);
            }
        }
        logger.info(uri);
        return uri;
    }

    public void setRemoteUri(MSetRestInfoRequest restInfo) {
        String mObjectId = restInfo.getRestInfoBean().getObjectId();
        String funcName = restInfo.getRestInfoBean().getFunctionName();
        String remoteInstanceId = restInfo.getRestInfoBean().getRestAddress();
        String instanceId = restInfo.getInstanceId();

        if (!this.remoteInstanceIdMap.containsKey(mObjectId)
                || !this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
            if (remoteInstanceId == null) {
                return;
            }
            this.addNewRemoteUri(instanceId, mObjectId, funcName, remoteInstanceId);
        } else if (remoteInstanceId == null) {
            this.deleteRemoteUri(instanceId, mObjectId, funcName);
        } else {
            this.replaceRemoteUri(instanceId, mObjectId, funcName, remoteInstanceId);
        }
    }

    private void addNewRemoteUri(String instanceId, String mObjectId, String funcName, String remoteInstanceId) {
        if (!this.remoteInstanceIdMap.containsKey(mObjectId)) {
            this.remoteInstanceIdMap.put(mObjectId, new HashMap<>());
        }

        this.remoteInstanceIdMap.get(mObjectId).put(funcName, remoteInstanceId);
        Optional<MServiceInstance> instanceOp = this.currModel.getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyAddNewRemoteUri(mServiceInstance.getId(), mObjectId, funcName));
    }

    private void replaceRemoteUri(String instanceId, String mObjectId, String funcName, String remoteInstanceId) {
        this.remoteInstanceIdMap.get(mObjectId).put(funcName, remoteInstanceId);
    }

    private void deleteRemoteUri(String instanceId, String mObjectId, String funcName) {
        this.remoteInstanceIdMap.get(mObjectId).remove(funcName);
        Optional<MServiceInstance> instanceOp = this.currModel.getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyDeleteRemoteUri(mServiceInstance.getId(), mObjectId, funcName));
    }

    // End -----------------------------------------------------
}
