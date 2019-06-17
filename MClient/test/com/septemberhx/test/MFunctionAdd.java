package com.septemberhx.test;

import mclient.annotation.MApiType;
import mclient.base.MObject;

/**
 * @Author: septemberhx
 * @Date: 2018-12-22
 * @Version 0.1
 */

public class MFunctionAdd extends MObject {

    @MApiType
    public int addInt(int a, int b) {
        return a + b;
    }

}
