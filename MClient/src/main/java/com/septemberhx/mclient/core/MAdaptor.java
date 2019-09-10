package com.septemberhx.mclient.core;

import com.septemberhx.mclient.base.MObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Version 0.1
 */
public class MAdaptor {

    private static volatile MAdaptor instance = new MAdaptor();
    private static Logger logger = LogManager.getLogger(MAdaptor.class);
    private Map<String, MObjectProxy> proxyMap;

    public static MAdaptor getInstance() {
        if (instance == null) {
            synchronized (MAdaptor.class) {
                if (instance == null) {
                    MAdaptor.instance = new MAdaptor();
                }
            }
        }
        return MAdaptor.instance;
    }

    private MAdaptor() {
        this.proxyMap = new HashMap<String, MObjectProxy>();
    }

    public static <T extends MObject> T getProxy(T target, MObject parent) {
        logger.debug(target.getId() + " --> " + parent.getId());
        logger.debug(target.getClass().toString() + " --> " + parent.getClass().toString());
        MObjectProxy mObjectProxy = new MObjectProxy();
        MAdaptor.getInstance().registerProxy(target.getId(), mObjectProxy);
        return (T) mObjectProxy.getInstance(target);
    }

    public static <T extends MObject> T getProxy(Class<T> tClass) {
        try {
            MObjectProxy mObjectProxy = new MObjectProxy();
            T tmpObject = tClass.newInstance();
            MAdaptor.getInstance().registerProxy(tmpObject.getId(), mObjectProxy);
            return tClass.cast(mObjectProxy.getInstance(tmpObject));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends MObject> T getProxy(Class<T> tClass, MObject parent) {
        T result = getProxy(tClass);
        if (result != null) {
            logger.debug(result.getId() + " --> " + parent.getId());
            logger.debug(tClass.toString() + " --> " + parent.getClass().toString());
        }
        return result;
    }

    public static MObject getProxy(String className) {
        try {
            MObjectProxy mObjectProxy = new MObjectProxy();
            MObject tmpObject = MObject.class.cast(Class.forName(className).newInstance());
            MAdaptor.getInstance().registerProxy(tmpObject.getId(), mObjectProxy);
            return MObject.class.cast(mObjectProxy.getInstance(tmpObject));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static MObject getProxy(String className, MObject parent) {
        MObject result = getProxy(className);
        if (result != null) {
            logger.debug(result.getId() + " --> " + parent.getId());
            logger.debug(className + " --> " + parent.getClass().toString());
        }
        return result;
    }

    private void registerProxy(String mObjectId, MObjectProxy mObjectProxy) {
        System.out.println("Resiger " + mObjectId);
        this.proxyMap.put(mObjectId, mObjectProxy);
    }
}
