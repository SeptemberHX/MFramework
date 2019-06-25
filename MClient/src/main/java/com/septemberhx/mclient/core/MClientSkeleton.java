package com.septemberhx.mclient.core;

import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.mclient.service.MClusterAgentClient;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

/**
 * @Author: septemberhx
 * @Date: 2019-06-12
 * @Version 0.1
 */
@Component
public class MClientSkeleton {

    private static volatile MClientSkeleton instance;
    @Getter
    private Map<String, MObject> mObjectMap;
    @Getter
    private Map<String, String> parentIdMap;
    @Getter
    private Map<String, Set<String>> objectId2ApiSet;

    @Autowired
    private MClusterAgentClient mClusterAgentClient;

    private Map<String, Map<String, MInstanceRestInfoBean>> restInfoMap;
    private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass());


    private MClientSkeleton() {
        this.mObjectMap = new HashMap<>();
        this.parentIdMap = new HashMap<>();
        this.objectId2ApiSet = new HashMap<>();
        this.restInfoMap = new HashMap<>();
    }

    public static MClientSkeleton getInstance() {
        if (instance == null) {
            synchronized (MClientSkeleton.class) {
                if (instance == null) {
                    instance = new MClientSkeleton();
                }
            }
        }
        return instance;
    }

    /*
     * register object
     */
    public void registerMObject(MObject object) {
        if (this.mObjectMap.containsKey(object.getId())) {
            logger.warn("MObject " + object.getId() + " has been registered before !!!");
        } else {
            this.mObjectMap.put(object.getId(), object);
        }
    }

    /*
     * register the parent id of object
     */
    public void registerParent(MObject object, String parentId) {
        if (this.mObjectMap.containsKey(object.getId())) {
            this.parentIdMap.put(object.getId(), parentId);
        } else {
            logger.warn("MObject " + object.getId() + " not registered");
        }
    }

    public void printParentIdMap() {
        logger.debug(this.parentIdMap.toString());
    }

    public List<String> getMObjectIdList() {
        return new ArrayList<>(this.mObjectMap.keySet());
    }

    /*
     * add an info bean
     */
    public void addRestInfo(MInstanceRestInfoBean infoBean) {
        if (infoBean.getRestAddress() == null) {
            this.removeRestInfo(infoBean);
            return;
        }

        if (!this.restInfoMap.containsKey(infoBean.getObjectId())) {
            this.restInfoMap.put(infoBean.getObjectId(), new HashMap<>());
        }
        this.restInfoMap.get(infoBean.getObjectId()).put(infoBean.getFunctionName(), infoBean);
    }

    /*
     * delete an info bean
     */
    private void removeRestInfo(MInstanceRestInfoBean infoBean) {
        if (this.restInfoMap.containsKey(infoBean.getObjectId())) {
            this.restInfoMap.get(infoBean.getObjectId()).remove(infoBean.getFunctionName());
        }
    }

    /**
     * Get all Rest info
     * @return List
     */
    public List<MInstanceRestInfoBean> getRestInfoBeanList() {
        List<MInstanceRestInfoBean> restInfoBeans = new ArrayList<>();
        for (String mObjectId : this.restInfoMap.keySet()) {
            restInfoBeans.addAll(this.restInfoMap.get(mObjectId).values());
        }
        return restInfoBeans;
    }

    /**
     * It will be used by MApiType annotation
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return boolean
     */
    public static boolean isRestNeeded(String mObjectId, String functionName) {
        return MClientSkeleton.getInstance().checkIfHasRestInfo(mObjectId, functionName);
    }

    /**
     * It will be used by MApiType annotation
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @param args: the arguments
     * @return Object
     */
    private static Object restRequest(String mObjectId, String functionName, Object... args) {
        URI uri = MClientSkeleton.getInstance().mClusterAgentClient.getRemoteUri(mObjectId, functionName);
        return null;
    }

    /**
     * request the information that needed by rest request for remote call
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return String
     */
    public String getRestInfo(String mObjectId, String functionName) {
        if (!this.checkIfHasRestInfo(mObjectId, functionName)) {
            throw new RuntimeException("Failed to fetch remote url for " + functionName + " in " + mObjectId);
        }
        return this.restInfoMap.get(mObjectId).get(functionName).getRestAddress();
    }

    /**
     * check whether need to use remote call or not
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return boolean
     */
    private boolean checkIfHasRestInfo(String mObjectId, String functionName) {
        return this.restInfoMap.containsKey(mObjectId) && this.restInfoMap.get(mObjectId).containsKey(functionName);
    }

    public void registerObjectAndApi(String mObjectId, String apiName) {
        if (!this.objectId2ApiSet.containsKey(mObjectId)) {
            this.objectId2ApiSet.put(mObjectId, new HashSet<>());
        }
        this.objectId2ApiSet.get(mObjectId).add(apiName);
    }
}
