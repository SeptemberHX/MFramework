package com.septemberhx.mclient.core;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.base.MObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;


/**
 * @Author: septemberhx
 * @Date: 2018-12-10
 * @Description: TODO
 * @Version 0.1
 */
public class MObjectProxy implements MethodInterceptor {

    private static Logger logger = Logger.getLogger(MObjectProxy.class);

    private MObject target;

    public MObjectProxy() {

    }

    public MObject getInstance(MObject mObject) {
        this.target = mObject;
        Enhancer en = new Enhancer();
        en.setSuperclass(mObject.getClass());
        en.setCallback(this);
        return (MObject)en.create();
    }

    public <T extends MObject> T getInstance(Class<T> tClass) throws IllegalAccessException, InstantiationException {
        this.target = tClass.newInstance();
        Enhancer en = new Enhancer();
        en.setSuperclass(tClass);
        en.setCallback(this);
        return tClass.cast(en.create());
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.getAnnotation(MApiFunction.class) != null) {
            System.out.println("=================== WE ARE HERE !!! ====================");
        }

        Object result = null;
        if (MClientSkeleton.isRestNeeded(this.target.getId(), method.getName())) {
            result = MClientSkeleton.restRequest(this.target.getId(), method.getName(), args);
        } else {
            result = methodProxy.invoke(target, args);
        }
        return result;
    }
}
