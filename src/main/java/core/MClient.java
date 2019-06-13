package core;

import base.MObject;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: septemberhx
 * @Date: 2019-06-12
 * @Version 0.1
 */
public class MClient {

    private static volatile MClient instance;
    private Map<String, MObject> mObjectMap;
    private Map<String, String> parentIdMap;
    private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass());

    private MClient() {
        this.mObjectMap = new HashMap<>();
        this.parentIdMap = new HashMap<>();
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

    public Map<String, String> getParentIdMap() {
        return parentIdMap;
    }
}
