package com.septemberhx.agent.controller;

import com.netflix.discovery.EurekaClient;
import com.septemberhx.agent.middleware.MServiceManager;
import com.septemberhx.agent.middleware.MServiceManagerEurekaImpl;
import com.septemberhx.agent.utils.MClientUtils;
import com.septemberhx.common.bean.MGetRemoteUriRequest;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@EnableAutoConfiguration
@RequestMapping("/magent")
public class MAgentController {

    @Value("${mclientagent.server.ip}")
    private String serverIpAddr;

    @Value("${mclientagent.server.port}")
    private Integer serverPort;

    @Autowired
    private MServiceManager clusterMiddleware;

    public MAgentController() {
    }

    @ResponseBody
    @RequestMapping(path = "/instanceInfoList", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfoList() {
        MInstanceInfoResponse response = new MInstanceInfoResponse();
        response.setInfoBeanList(this.clusterMiddleware.getInstanceInfoList());
        return response;
    }

    @ResponseBody
    @RequestMapping(path = "/deleteInstance", method = RequestMethod.GET)
    public void deleteInstance(@RequestParam("dockerInstanceId") String instanceId) {
        MClientUtils.deleteInstanceById(instanceId);
    }

    @ResponseBody
    @RequestMapping(path = "/remoteuri", method = RequestMethod.POST)
    public URI getRemoteUri(@RequestBody MGetRemoteUriRequest remoteUriRequest) {
        URI serverRemoteUri = MUrlUtils.getMServerRemoteUri(this.serverIpAddr, this.serverPort);
        return MRequestUtils.sendRequest(serverRemoteUri, remoteUriRequest, URI.class, RequestMethod.POST);
    }

    @ResponseBody
    @RequestMapping(path = "/setRestInfo", method = RequestMethod.POST)
    public void setRemoteUri(@RequestBody MSetRestInfoRequest mSetRestInfoRequest) {
        MInstanceInfoBean infoBean = this.clusterMiddleware.getInstanceInfoById(mSetRestInfoRequest.getInstanceId());
        MClientUtils.sendRestInfo(
                MUrlUtils.getMClusterSetRestInfoUri(infoBean.getIp(), infoBean.getPort()),
                mSetRestInfoRequest.getRestInfoBean());
    }
}
