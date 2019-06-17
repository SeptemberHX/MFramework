package mclient.base;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import mclient.annotation.MApiType;
import mclient.annotation.MFunctionType;
import mclient.annotation.MServiceType;
import mclient.core.MClient;
import mclient.core.MObjectProxy;
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
    private MObject objectProxy;

    protected MObject() {
        this.id = this.getClass().getCanonicalName() + UUID.randomUUID().toString();

        if (this.getClass().getAnnotation(MServiceType.class) != null) {
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get(this.getClass().getName());
                for (CtMethod method : ctClass.getMethods()) {
                    if (method.getAnnotation(MApiType.class) != null) {
                        logger.debug(method.getName());
                        method.setBody("return \"Modified by Javassist!\";");
                    }
                }
                ctClass.toClass();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // exclude the cglib object
        if (this.getId() != null) {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getAnnotation(MFunctionType.class) != null) {
                    if (!MObject.class.isAssignableFrom(field.getType())) {
                        throw new IllegalArgumentException("Wrong type with MFunctionType mclient.annotation!");
                    } else {

                        try {
                            MObjectProxy tmpProxy = new MObjectProxy();

                            // create an instance of field.getType with default constructor
                            // this means the class needs to have an default constructor
                            Class<?> clazz = field.getType();
                            Constructor<?> ctor = clazz.getConstructor();
                            MObject obj = (MObject) ctor.newInstance(new Object[]{});
                            MClient.getInstance().registerParent(obj, this.getId());

                            // set the proxy object
                            field.setAccessible(true);
                            field.set(this, tmpProxy.getInstance((obj)));
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
            MClient.getInstance().registerMObject(this);
            logger.debug(this.getId() + " created");
            MClient.getInstance().printParentIdMap();
        }
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }
}
