package com.septemberhx.mclient.utils;

import java.util.UUID;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Version 0.1
 */
public class StringUtils {

    public static String generateMResourceId() {
        return "MResource_" + UUID.randomUUID();
    }

    public static String generateMFunctionId() {
        return "MFunction_" + UUID.randomUUID();
    }

    public static String generateMBusinessId() {
        return "MBusiness_" + UUID.randomUUID();
    }
}
