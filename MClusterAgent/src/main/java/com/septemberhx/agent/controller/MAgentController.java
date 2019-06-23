package com.septemberhx.agent.controller;

import com.netflix.discovery.EurekaClient;
import com.septemberhx.agent.middleware.MDockerManager;
import com.septemberhx.agent.middleware.MServiceManager;
import com.septemberhx.agent.middleware.MServiceManagerEurekaImpl;
import com.septemberhx.agent.middleware.MServiceManagerK8SImpl;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@EnableAutoConfiguration
@RequestMapping("/magent")
public class MAgentController {

    @Autowired
    private EurekaClient discoveryClient;

    private MServiceManager clusterMiddleware;

    public MAgentController() {
        this.clusterMiddleware = new MServiceManagerEurekaImpl();
    }

    @ResponseBody
    @RequestMapping(path = "/instanceInfoList", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfoList() {
        this.stupidCheck();

        MInstanceInfoResponse response = new MInstanceInfoResponse();
        response.setInfoBeanList(this.clusterMiddleware.getInstanceInfoList());
        return response;
    }

    private void stupidCheck() {
        if (this.clusterMiddleware instanceof MServiceManagerEurekaImpl) {
            MServiceManagerEurekaImpl eurekaImpl = (MServiceManagerEurekaImpl) this.clusterMiddleware;
            if (eurekaImpl.getDiscoveryClient() == null) {
                eurekaImpl.setDiscoveryClient(this.discoveryClient);
            }
        }
    }
}
