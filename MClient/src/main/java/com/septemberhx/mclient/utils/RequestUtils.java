package com.septemberhx.mclient.utils;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Description: TODO
 * @Version 0.1
 */
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
}
