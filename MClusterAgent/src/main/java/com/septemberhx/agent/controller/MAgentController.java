package com.septemberhx.agent.controller;

import com.septemberhx.agent.middleware.MClusterMiddlewareInterface;
import com.septemberhx.agent.middleware.MK8SMiddleware;
import com.septemberhx.common.bean.MInstanceInfoBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<MInstanceInfoBean> getInstanceInfoList() {
        return this.clusterMiddleware.getInstanceInfoList();
    }
}
