package com.septemberhx.mclient.http;

import com.septemberhx.mclient.base.MObject;
import org.json.JSONObject;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: septemberhx
 * @Date: 2018-12-24
 * @Version 0.1
 */
public class MHttpProxy {

    private MObject mObject;

    public <T extends MObject> MHttpProxy(Class<T> clazz) {
        try {
            this.mObject = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String execute(String methodName, JSONObject jsonObject) {
        try {
            // todo: deal with overload functions(functions that have the same name but different parameters)
            Method targetMethod = null;
            for (Method method : this.mObject.getClass().getMethods()) {
                if (method.getName().equals(methodName)) {
                    targetMethod = method;
                    break;
                }
            }

            DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
            String[] names = parameterNameDiscoverer.getParameterNames(targetMethod);
            if (names != null && names.length > 0) {
                List<Object> argList = new ArrayList<>();
                for (String name : names) {
                    argList.add(jsonObject.get(name));
                }
                return JSONObject.valueToString(targetMethod.invoke(this.mObject, argList.toArray()));
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return null;
    }
}
