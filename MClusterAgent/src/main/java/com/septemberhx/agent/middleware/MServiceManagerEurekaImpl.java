package com.septemberhx.agent.middleware;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.RequestUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class MServiceManagerEurekaImpl implements MServiceManager {

    @Getter
    @Setter
    private EurekaClient discoveryClient;
    private MDockerManager dockerManager;

    public MServiceManagerEurekaImpl() {
        this.dockerManager = new MDockerManagerK8SImpl();
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
    public List<MInstanceInfoBean> getInstanceInfoList() {
        List<MInstanceInfoBean> resultList = new ArrayList<>();

//        for (String service : this.discoveryClient.getServices()) {
//            for (ServiceInstance instanceInfo : this.discoveryClient.getInstances(service)) {
//                MInstanceInfoBean instanceInfoBean = new MInstanceInfoBean();
//                instanceInfoBean.setId(instanceInfo.getInstanceId());
//                instanceInfoBean.setNodeId(instanceInfo.getHost());
//                instanceInfoBean.setPort(instanceInfo.getPort());
//
////                MClientInfoBean response = RequestUtils.sendRequest(
////                        MUrlUtils.getMClusterAgentFetchClientInfoUri(instanceInfo.get, instanceInfo.getPort()),
////                        null,
////                        MClientInfoBean.class,
////                        RequestMethod.GET
////                );
////                instanceInfoBean.setParentIdMap(response.getParentIdMap());
////                instanceInfoBean.setApiMap(response.getApiMap());
//                resultList.add(instanceInfoBean);
//            }
//        }
        for (Application application : this.discoveryClient.getApplications().getRegisteredApplications()) {
            for (InstanceInfo instanceInfo : application.getInstances()) {
                MInstanceInfoBean instanceInfoBean = new MInstanceInfoBean();
                instanceInfoBean.setId(instanceInfo.getId());
                instanceInfoBean.setIp(instanceInfo.getIPAddr());
                instanceInfoBean.setPort(instanceInfo.getPort());

                MClientInfoBean response = RequestUtils.sendRequest(
                        MUrlUtils.getMClusterAgentFetchClientInfoUri(instanceInfo.getIPAddr(), instanceInfo.getPort()),
                        null,
                        MClientInfoBean.class,
                        RequestMethod.GET
                );

                // when the application is not supported by our framework, just jump over it.
                if (response == null) {
                    continue;
                }

                instanceInfoBean.setParentIdMap(response.getParentIdMap());
                instanceInfoBean.setApiMap(response.getApiMap());
                instanceInfoBean.setDockerInfo(dockerManager.getDockerInfoByIpAddr(instanceInfo.getIPAddr()));
                resultList.add(instanceInfoBean);
            }
        }

        return resultList;
    }
}
