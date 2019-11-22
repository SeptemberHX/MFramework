package com.septemberhx.agent.controller;

import com.netflix.appinfo.InstanceInfo;
import com.septemberhx.agent.utils.ElasticSearchUtils;
import com.septemberhx.agent.utils.MClientUtils;
import com.septemberhx.common.base.MUpdateCacheBean;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.bean.*;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@EnableAutoConfiguration
@RequestMapping("/magent")
public class MAgentController {

    private static Logger logger = LogManager.getLogger(MAgentController.class);

    @Value("${mclientagent.server.ip}")
    private String serverIpAddr;

    @Value("${mclientagent.server.port}")
    private Integer serverPort;

    @Value("${mclientagent.elasticsearch.ip}")
    private String elasticsearchIpAddr;

    @Value("${mclientagent.elasticsearch.port}")
    private Integer elasticsearchPort;

    @Autowired
    private MClientUtils clientUtils;

    private RestHighLevelClient esClient;

    public MAgentController() {
    }

    @PostConstruct
    public void init() throws IOException {
        logger.info("Elasticsearch: " + elasticsearchIpAddr + ":" + elasticsearchPort);
        this.esClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(this.elasticsearchIpAddr, this.elasticsearchPort)
                )
        );
    }

    @ResponseBody
    @RequestMapping(path = "/fetchRequestUrl", method = RequestMethod.POST)
    public String fetchRequestUrl(@RequestBody MUserDemand userDemand) {
        URI requestUri = MUrlUtils.getMServerFetchRequestUrl(this.serverIpAddr, this.serverPort);
        return MRequestUtils.sendRequest(requestUri, userDemand, String.class, RequestMethod.POST);
    }

    @ResponseBody
    @RequestMapping(path = "/updateGateways", method = RequestMethod.POST)
    public void updateGateway(@RequestBody MUpdateCacheBean cacheBean) {
        for (InstanceInfo info : this.clientUtils.getAllGatewayInstance()) {
            URI uri = MUrlUtils.getMGatewayUpdateUri(info.getIPAddr(), info.getPort());
            System.out.println(uri.toString());
            MRequestUtils.sendRequest(uri, cacheBean, null, RequestMethod.POST);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/instanceInfoList", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfoList() {
        MInstanceInfoResponse response = new MInstanceInfoResponse();
        response.setInfoBeanList(this.clientUtils.getInstanceInfoList());
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
        MInstanceInfoBean infoBean = this.clientUtils.getInstanceInfoById(mSetRestInfoRequest.getInstanceId());
        MClientUtils.sendRestInfo(
                MUrlUtils.getMClusterSetRestInfoUri(infoBean.getIp(), infoBean.getPort()),
                mSetRestInfoRequest.getRestInfoBean());
    }

    @ResponseBody
    @RequestMapping(path = "/deploy", method = RequestMethod.POST)
    public void deploy(@RequestBody MDeployPodRequest mDeployPodRequest) {
        this.clientUtils.depoly(mDeployPodRequest);
    }

    @ResponseBody
    @RequestMapping(path = "/registered", method = RequestMethod.POST)
    public void instanceRegistered(@RequestBody MInstanceRegisterNotifyRequest registerNotifyRequest) {
        InstanceInfo instanceInfo = registerNotifyRequest.getInstanceInfo();
        System.out.println(instanceInfo.getAppName() + "|" + instanceInfo.getInstanceId() + "|" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort());
        MInstanceInfoBean infoBean = this.clientUtils.transformInstance(instanceInfo, registerNotifyRequest.getPort());
        if (infoBean == null) {
            return;
        }

        URI serverLoadUri = MUrlUtils.getMServerLoadInstanceInfoUri(this.serverIpAddr, this.serverPort);
        logger.info(infoBean.toString());

        try {
            MRequestUtils.sendRequest(serverLoadUri, infoBean, null, RequestMethod.POST);
            this.clientUtils.notifyDeployJobFinished(infoBean);
        } catch (Exception e) {
            logger.warn("Failed to notify server with data in MAgentController::instanceRegistered");
        }
    }

    @RequestMapping(path = "/setApiContinueStatus", method = RequestMethod.POST)
    public void setApiContinueStatus(@RequestBody MS2CSetApiCStatus ms2CSetApiCStatus) {
        MInstanceInfoBean infoBean = this.clientUtils.getInstanceInfoById(ms2CSetApiCStatus.getInstanceId());
        MRequestUtils.sendRequest(MUrlUtils.getMClientSetApiCStatus(infoBean.getIp(), infoBean.getPort()), ms2CSetApiCStatus.getApiContinueRequest(), null, RequestMethod.POST);
    }

    @RequestMapping(path = "/fetchLogsBetweenTime", method = RequestMethod.POST)
    public MFetchLogsResponse fetchLogsBetweenTime(@RequestBody MFetchLogsBetweenTimeRequest request) {
        MFetchLogsResponse response = new MFetchLogsResponse();
        response.setLogList(ElasticSearchUtils.getLogsBetween(
                this.esClient,
                new String[]{"logstash-*"},
                request.getStartTime(),
                request.getEndTime()
        ));
        return response;
    }
}
