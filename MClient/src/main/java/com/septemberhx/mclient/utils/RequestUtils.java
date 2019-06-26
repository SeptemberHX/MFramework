package com.septemberhx.mclient.utils;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Description: TODO
 * @Version 0.1
 */
@Component
public class RequestUtils {

    public static Logger logger = Logger.getLogger(RequestUtils.class);

    public static String methodParamToJsonString(Method method, Object[] args) {
        String resultJsonStr = "";
        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

        String[] names = parameterNameDiscoverer.getParameterNames(method);
        if (names != null) {
            if (names.length != args.length) {
                throw new InvalidParameterException("args' size don't match");
            } else {
                JSONObject resultJson = new JSONObject();
                for (int i = 0; i < names.length; ++i) {
                    resultJson.put(names[i], args[i]);
                }
                resultJsonStr = resultJson.toString(4);
            }
        }
        return resultJsonStr;
    }

    public static String methodParamToJsonString(List<String> names, List<Object> values) {
        JSONObject resultJson = new JSONObject();
        for (int i = 0; i < names.size(); ++i) {
            resultJson.put(names.get(i), values.get(i));
        }
        return resultJson.toString(4);
    }
}
