package com.septemberhx.mclient.core;

import com.septemberhx.mclient.base.MObject;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @Author: septemberhx
 * @Date: 2019-06-12
 * @Version 0.1
 */
public class MClient {

    private static volatile MClient instance;
    @Getter
    private Map<String, MObject> mObjectMap;
    @Getter
    private Map<String, String> parentIdMap;
    @Getter
    private Map<String, Set<String>> objectId2ApiSet;
    private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass());

    private MClient() {
        this.mObjectMap = new HashMap<>();
        this.parentIdMap = new HashMap<>();
        this.objectId2ApiSet = new HashMap<>();
    }

    public static MClient getInstance() {
        if (instance == null) {
            synchronized (MClient.class) {
                if (instance == null) {
                    instance = new MClient();
                }
            }
        }
        return instance;
    }

    public void registerMObject(MObject object) {
        if (this.mObjectMap.containsKey(object.getId())) {
            logger.warn("MObject " + object.getId() + " has been registered before !!!");
        } else {
            this.mObjectMap.put(object.getId(), object);
        }
    }

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

    public static boolean isRestNeeded(String mObjectId, String functionName) {
        return false;
    }

    public static Object restRequest(String mObjectId, String functioName, Object... args) {

        return null;
    }

    public void registerObjectAndApi(String mObjectId, String apiName) {
        if (!this.objectId2ApiSet.containsKey(mObjectId)) {
            this.objectId2ApiSet.put(mObjectId, new HashSet<>());
        }
        this.objectId2ApiSet.get(mObjectId).add(apiName);
    }
}
