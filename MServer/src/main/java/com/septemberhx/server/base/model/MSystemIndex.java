package com.septemberhx.server.base.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/16
 *
 * This class is used to restore the important indices of the whole service system
 */
@Getter
@Setter
public class MSystemIndex {
    private Map<String, Double> userId2AvgResTimeEachReq;       // average response time of each request in mills

    public MSystemIndex() {
        this.userId2AvgResTimeEachReq = new HashMap<>();
    }
}
