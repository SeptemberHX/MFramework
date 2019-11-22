package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
@Getter
@Setter
public class MUpdateCacheBean {
    private Map<String, String> demandId2Url;

    public MUpdateCacheBean() {
        demandId2Url = new HashMap<>();
    }

    public MUpdateCacheBean(Map<String, String> data) {
        this.demandId2Url = data;
    }

    @Override
    public String toString() {
        return "MUpdateCacheBean{" +
                "demandId2Url=" + demandId2Url +
                '}';
    }
}
