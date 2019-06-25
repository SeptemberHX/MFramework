package com.septemberhx.agent.controller;

import com.netflix.discovery.EurekaClient;
import com.septemberhx.agent.middleware.MServiceManager;
import com.septemberhx.agent.middleware.MServiceManagerEurekaImpl;
import com.septemberhx.agent.utils.MClientUtils;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


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

    @ResponseBody
    @RequestMapping(path = "/deleteInstance", method = RequestMethod.GET)
    public void deleteInstance(@RequestParam("dockerInstanceId") String instanceId) {
        MClientUtils.deleteInstanceById(instanceId);
    }

    @ResponseBody
    @RequestMapping(path = "/remoteuri", method = RequestMethod.GET)
    public URI getRemoteUri(@RequestParam("objectId") String mObjectId, @RequestParam("functionName") String funcName) {
        return null;
    }


    @ResponseBody
    @RequestMapping(path = "/setRestInfo", method = RequestMethod.POST)
    public void setRemoteUri(@RequestParam("restInfo")MSetRestInfoRequest mSetRestInfoRequest) {
        this.stupidCheck();

        MInstanceInfoBean infoBean = this.clusterMiddleware.getInstanceInfoById(mSetRestInfoRequest.getInstanceId());
        MClientUtils.sendRestInfo(
                MUrlUtils.getMClusterSetRestInfoUri(infoBean.getIp(), infoBean.getPort()),
                mSetRestInfoRequest.getRestInfoBean());
    }
}
