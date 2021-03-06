package com.septemberhx.server.core;


import com.septemberhx.common.base.*;
import com.septemberhx.common.bean.MGetRemoteUriRequest;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.job.MJobManager;
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

    @Getter
    private MJobManager jobManager = new MJobManager();

    @Getter
    private MRepoManager repoManager;

    private MServerSkeleton() {
        this.remoteInstanceIdMap = new HashMap<>();
    }

    public static String fetchRequestUrl(String demandId, ServerNodeType requesterType) {
        Optional<MDemandState> demandStateOptional = MSystemModel.getIns().getDemandStateManager().getById(demandId);
        if (demandStateOptional.isPresent()) {
            MDemandState demandState = demandStateOptional.get();
            Optional<MServiceInstance> instanceOptional = MSystemModel.getIns().getInstanceById(demandState.getInstanceId());
            if (instanceOptional.isPresent()) {
                MServiceInstance serviceInstance = instanceOptional.get();
                MServerNode targetNode = MSystemModel.getIns().getMSNManager().getById(serviceInstance.getNodeId()).get();

                // if the instance is on the cloud side, and it is the edge side that asks for the url
                // then we should tell the requester that you should send the request to the cloud
                if (targetNode.getNodeType() == ServerNodeType.CLOUD && requesterType == ServerNodeType.EDGE) {
                    return MClusterConfig.REQUEST_SHOULD_SEND_TO_CLOUD;
                }
                MServiceInterface mServiceInterface = MSystemModel.getIns().getServiceManager().getInterfaceById(demandState.getInterfaceId());

                String patternUrl = mServiceInterface.getPatternUrl();
                if (!patternUrl.startsWith("/")) {
                    patternUrl = "/" + patternUrl;
                }
                URI uri = MUrlUtils.getRemoteUri(serviceInstance.getIp(), serviceInstance.getPort(), patternUrl);
                return uri.toString();
            }
        }
        return null;
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
        MSystemModel.getIns().loadInstanceInfo(infoBean);
    }

    public Optional<MServiceInstance> getInstanceInfo(String instanceId) {
        return MSystemModel.getIns().getInstanceById(instanceId);
    }

    public List<MServiceInstance> getAllInstanceInfos() {
        return MSystemModel.getIns().getAllServiceInstance();
    }

    // Remote Uri stuffs below ---------------------------------

    public URI getRemoteUri(MGetRemoteUriRequest remoteUriRequest) {
        logger.debug(remoteUriRequest);
        String mObjectId = remoteUriRequest.getObjectId();
        String funcName = remoteUriRequest.getFunctionName();
        URI uri = null;

        if (this.remoteInstanceIdMap.containsKey(mObjectId)
                && this.remoteInstanceIdMap.get(mObjectId).containsKey(funcName)) {
            Optional<MServiceInstance> instance = MSystemModel.getIns().getInstanceById(
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
        Optional<MServiceInstance> instanceOp = MSystemModel.getIns().getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyAddNewRemoteUri(mServiceInstance.getId(), mObjectId, funcName));
    }

    private void replaceRemoteUri(String instanceId, String mObjectId, String funcName, String remoteInstanceId) {
        this.remoteInstanceIdMap.get(mObjectId).put(funcName, remoteInstanceId);
    }

    private void deleteRemoteUri(String instanceId, String mObjectId, String funcName) {
        this.remoteInstanceIdMap.get(mObjectId).remove(funcName);
        Optional<MServiceInstance> instanceOp = MSystemModel.getIns().getInstanceByMObjectId(mObjectId);
        instanceOp.ifPresent(
                mServiceInstance -> MServerUtils.notifyDeleteRemoteUri(mServiceInstance.getId(), mObjectId, funcName));
    }

    // End -----------------------------------------------------
}
