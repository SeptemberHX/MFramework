package core;

import base.MCallType;
import base.MObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import utils.RequestUtils;

import java.lang.reflect.Method;


/**
 * @Author: septemberhx
 * @Date: 2018-12-10
 * @Description: TODO
 * @Version 0.1
 */
public class MObjectProxy implements MethodInterceptor {

    private static Logger logger = Logger.getLogger(MObjectProxy.class);

    private Object target;
    private MCallType callType;

    public MObjectProxy() {
        this.callType = MCallType.OBJECT;
    }

    public Object getInstance(MObject mObject) {
        this.target = mObject;
        Enhancer en = new Enhancer();
        en.setSuperclass(mObject.getClass());
        en.setCallback(this);
        return en.create();
    }

    public <T extends MObject> T getInstance(Class<T> tClass) throws IllegalAccessException, InstantiationException {
        this.target = tClass.newInstance();
        Enhancer en = new Enhancer();
        en.setSuperclass(tClass);
        en.setCallback(this);
        return tClass.cast(en.create());
    }

    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result = null;
        switch (this.callType) {
            case OBJECT:
                result = methodProxy.invoke(target, args);
                break;
            case REST:
                logger.info(((MObject)this.target).getId());
                logger.info(method.getName());
                logger.info(RequestUtils.methodParamToJsonString(method, args));
                break;
            default:
                break;
        }
        return result;
    }

    // Getters and Setters below

    public MCallType getCallType() {
        return callType;
    }

    public void setCallType(MCallType callType) {
        this.callType = callType;
    }
}
