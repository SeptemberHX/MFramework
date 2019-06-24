package com.septemberhx.sampleservice.utils;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.base.MObject;

import java.net.InetAddress;

public class MCalcUtils extends MObject {

    @MApiFunction
    public String wrapper(String rawStr) {
        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }

    @MApiFunction
    public String upper(String rawStr) {
        return rawStr.toUpperCase();
    }
}
