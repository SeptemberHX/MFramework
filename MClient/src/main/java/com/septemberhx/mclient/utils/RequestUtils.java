package com.septemberhx.mclient.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Description: TODO
 * @Version 0.1
 */
@Component
public class RequestUtils {

    public static Logger logger = LogManager.getLogger(RequestUtils.class);

    public static List<String> getMethodParamNames(Method method) {
        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] names = parameterNameDiscoverer.getParameterNames(method);
        List<String> nameList = new ArrayList<>();
        if (names != null) {
            nameList.addAll(Arrays.asList(names));
        }
        return nameList;
    }

    public static String methodParamToJsonString(List<String> names, List<Object> values) {
        JSONObject resultJson = new JSONObject();
        for (int i = 0; i < names.size(); ++i) {
            resultJson.put(names.get(i), values.get(i));
        }
        return resultJson.toString(4);
    }
}
