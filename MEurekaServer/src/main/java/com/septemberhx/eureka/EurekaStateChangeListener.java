package com.septemberhx.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MInstanceRegisterNotifyRequest;
import com.septemberhx.eureka.client.MClusterAgentClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaStateChangeListener {

    private static Logger logger = LogManager.getLogger(EurekaStateChangeListener.class);

    @Qualifier("eurekaClient")
    @Autowired
    EurekaClient eurekaClient;

    @Autowired
    private MClusterAgentClient clusterAgentClient;

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
        if (!instanceInfo.getAppName().equalsIgnoreCase(MClusterConfig.MCLUSTERAGENT_NAME) && instanceInfo.getPort() != 0) {
            try {
                MInstanceRegisterNotifyRequest notifyRequest = new MInstanceRegisterNotifyRequest();
                notifyRequest.setIp(instanceInfo.getIPAddr());
                notifyRequest.setPort(instanceInfo.getPort());
                notifyRequest.setInstanceInfo(instanceInfo);
                this.clusterAgentClient.instanceRegistered(notifyRequest);
                logger.info(instanceInfo);
            } catch (Exception e) {
                logger.debug(e);
                e.printStackTrace();
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