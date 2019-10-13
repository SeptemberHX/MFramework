package com.septemberhx.server.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
class MIDUtilsTest {

    @Test
    void generateInstanceId() {
        String nodeId = "node1";
        String serviceId = "service1";
        List<String> currentIdList = new LinkedList<>();

        String result1 = MIDUtils.generateInstanceId(nodeId, serviceId, currentIdList);
        assertEquals("node1_service1_0000", result1);

        currentIdList.add(result1);
        String result2 = MIDUtils.generateInstanceId(nodeId, serviceId, currentIdList);
        assertEquals("node1_service1_0001", result2);

        currentIdList.add(result2);
        currentIdList.add("node1_service1_3");
        String result3 = MIDUtils.generateInstanceId(nodeId, serviceId, currentIdList);
        assertEquals("node1_service1_0002", result3);

        currentIdList.add(result3);
        currentIdList.add(0, "node1_service1_0004");
        String result4 = MIDUtils.generateInstanceId(nodeId, serviceId, currentIdList);
        assertEquals("node1_service1_0005", result4);

        Collections.sort(currentIdList);
        assertEquals("node1_service1_0002", currentIdList.get(2));
    }
}