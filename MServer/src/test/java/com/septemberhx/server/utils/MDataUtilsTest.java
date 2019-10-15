package com.septemberhx.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/15
 */
class MDataUtilsTest {

    @Test
    void loadDataFromDir() {
        MDataUtils.loadDataFromDir("D:\\Workspace\\gitlab\\mdata\\Lab2\\TestData");
    }
}