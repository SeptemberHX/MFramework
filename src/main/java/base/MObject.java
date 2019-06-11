package base;

import annotation.MFunctionType;
import core.MAdaptor;
import core.MMain;
import core.MObjectProxy;
import http.MHttpProxy;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @Author: septemberhx
 * @Date: 2018-12-09
 * @Version 0.2
 */
public abstract class MObject {
    private Logger logger = Logger.getLogger(this.getClass());
    private String id = null;

    protected MObject() {
        logger.debug(this.getClass());

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getAnnotation(MFunctionType.class) != null) {
                MFunctionType mFunctionType = field.getAnnotation(MFunctionType.class);
                logger.debug(mFunctionType.type());
                logger.debug(field.getName());
                logger.debug(field.getType());

                if (!MObject.class.isAssignableFrom(field.getType())) {
                    throw new IllegalArgumentException("Wrong type with MFunctionType annotation!");
                } else {

                    try {
                        MObjectProxy tmpProxy = new MObjectProxy();

                        // create an instance of field.getType with default constructor
                        // this means the class needs to have an default constructor
                        Class<?> clazz = field.getType();
                        Constructor<?> ctor = clazz.getConstructor();
                        Object obj = ctor.newInstance(new Object[]{});

                        // set the proxy object
                        field.setAccessible(true);
                        field.set(this, tmpProxy.getInstance((MObject) obj));
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (InstantiationException e2) {
                        e2.printStackTrace();
                    } catch (NoSuchMethodException e3) {
                        e3.printStackTrace();
                    } catch (InvocationTargetException e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }
}
