package base;

import core.MAdaptor;
import core.MMain;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @Author: septemberhx
 * @Date: 2018-12-09
 * @Version 0.1
 */
public abstract class MObject {
    private Logger logger = Logger.getLogger(this.getClass());
    private String id = null;

    protected MObject() {
        MMain mMain = MMain.getInstance();

        try {
            for (Pair<String, String> fieldPair : mMain.getMObjectFieldPairList(this.getClass())) {
                String filedName = fieldPair.getKey().substring(0, 1).toUpperCase() + fieldPair.getKey().substring(1);
                Method method = this.getClass().getMethod("set" + filedName, Class.forName(fieldPair.getValue()));
                if (method != null) {
                    method.invoke(this, MAdaptor.getProxy(fieldPair.getValue(), this));
                    logger.debug("Set " + this.getClass().getName() + "#" + fieldPair.getKey() + " to " + fieldPair.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }
}
