package com.septemberhx.agent.controller;

import com.septemberhx.agent.middleware.MClusterMiddlewareInterface;
import com.septemberhx.agent.middleware.MK8SMiddleware;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@EnableAutoConfiguration
@RequestMapping("/magent")
public class MAgentController {

    private MClusterMiddlewareInterface clusterMiddleware;

    public MAgentController() {
        this.clusterMiddleware = new MK8SMiddleware("http://192.168.1.102:8082");
    }

    @ResponseBody
    @RequestMapping(path = "/instanceInfoList", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfoList() {
        MInstanceInfoResponse response = new MInstanceInfoResponse();
        response.setInfoBeanList(this.clusterMiddleware.getInstanceInfoList());
        return response;
    }
}
