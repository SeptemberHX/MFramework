package com.septemberhx.agent.controller;

import com.septemberhx.agent.middleware.MClusterMiddlewareInterface;
import com.septemberhx.agent.middleware.MK8SMiddleware;

import java.util.List;

public class MAgentController {

    private MClusterMiddlewareInterface clusterMiddleware;

    public MAgentController() {
        this.clusterMiddleware = new MK8SMiddleware("http://192.168.1.102:8082");
    }

    private List<String> getPodIdList() {

    }
}
