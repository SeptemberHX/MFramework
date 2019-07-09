package com.septemberhx.agent.middleware;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MDeployPodRequest;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.MRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Component
public class MServiceManagerEurekaImpl implements MServiceManager {

    @Autowired
    private EurekaClient discoveryClient;

    public MServiceManagerEurekaImpl() {
    }

    @Override
    public Set<String> getNodeIdSet() {
        return null;
    }

    @Override
    public Optional<String> getInstanceIdByIp(String ip) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNodeIdOfInstance(String instanceId) {
        return Optional.empty();
    }

    @Override
    public List<InstanceInfo> getInstanceInfoList() {
        List<InstanceInfo> resultList = new ArrayList<>();

        for (Application application : this.discoveryClient.getApplications().getRegisteredApplications()) {
            // when the application is not supported by our framework, just jump over it.
            //                MInstanceInfoBean instanceInfoBean = this.transformInstance(instanceInfo);
            //                if (instanceInfoBean != null) {
            //                    resultList.add(instanceInfoBean);
            //                }
            resultList.addAll(application.getInstances());
        }

        return resultList;
    }

    @Override
    public InstanceInfo getInstanceInfoById(String instanceId) {
        List<InstanceInfo> instanceInfos = this.discoveryClient.getInstancesById(instanceId);
        if (instanceInfos.size() > 0) {
            return instanceInfos.get(0);
        } else {
            return null;
        }
    }

    @Override
    public InstanceInfo getInstanceInfoByIpAndPort(String ipAddr) {
        System.out.println("App size = " + this.discoveryClient.getApplications().getRegisteredApplications().size());
        for (Application application : this.discoveryClient.getApplications().getRegisteredApplications()) {
            System.out.println(application.getName());
            for (InstanceInfo instanceInfo : application.getInstances()) {
                System.out.println(instanceInfo.getAppName() + "|" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort());
                if (instanceInfo.getIPAddr().equals(ipAddr)) {
                    return instanceInfo;
                }
            }
        }
        return null;
    }
}
