package com.septemberhx.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MInstanceRegisterNotifyRequest;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

/**
 * Created by wuweifeng on 2017/10/10.
 */
@Component
public class EurekaStateChangeListener {

    private static Logger logger = LogManager.getLogger(EurekaStateChangeListener.class);

    @Qualifier("eurekaClient")
    @Autowired
    EurekaClient eurekaClient;

    @Value("${mcluster.agent.name}")
    private String clusterAgentName;

    @Value("${mcluster.agent.ip}")
    private String clusterAgentIp;

    @Value("${mcluster.agent.port}")
    private int clusterAgentPort;

    @EventListener
    public void listen(EurekaInstanceCanceledEvent eurekaInstanceCanceledEvent) {
        //服务断线事件
        String appName = eurekaInstanceCanceledEvent.getAppName();
        String serverId = eurekaInstanceCanceledEvent.getServerId();
        System.out.println(appName);
        System.out.println(serverId);
    }

    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        System.out.println(instanceInfo.getAppName()+"|"+instanceInfo.getInstanceId()+"|"+instanceInfo.getIPAddr()+"|"+instanceInfo.getPort());
        if (!instanceInfo.getAppName().equals(clusterAgentName.toUpperCase()) && instanceInfo.getPort() != 0) {
            try {
                URI uri = MUrlUtils.getRemoteUri(clusterAgentIp, clusterAgentPort, "/magent/registered");
                MInstanceRegisterNotifyRequest notifyRequest = new MInstanceRegisterNotifyRequest();
                notifyRequest.setIp(instanceInfo.getIPAddr());
                notifyRequest.setPort(instanceInfo.getPort());
                notifyRequest.setInstanceInfo(instanceInfo);
                MRequestUtils.sendRequest(uri, notifyRequest, null, RequestMethod.POST);
                logger.info(instanceInfo);
            } catch (Exception e) {
                logger.warn("Failed to connect to MClusterAgent!");
            }
        }
    }

    @EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
        event.getAppName();
        event.getServerId();
    }

    @EventListener
    public void listen(EurekaRegistryAvailableEvent event) {

    }

    @EventListener
    public void listen(EurekaServerStartedEvent event) {
        //Server启动
    }
}