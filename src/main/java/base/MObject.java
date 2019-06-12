package base;

import annotation.MFunctionType;
import core.MClient;
import core.MObjectProxy;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        this.id = this.getClass().getCanonicalName() + UUID.randomUUID().toString();

        if (this.getId() != null) {  // exclude the cglib object
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getAnnotation(MFunctionType.class) != null) {
                    MFunctionType mFunctionType = field.getAnnotation(MFunctionType.class);
//                logger.debug(mFunctionType.type());
//                logger.debug(field.getName());
//                logger.debug(field.getType());

                    if (!MObject.class.isAssignableFrom(field.getType())) {
                        throw new IllegalArgumentException("Wrong type with MFunctionType annotation!");
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
