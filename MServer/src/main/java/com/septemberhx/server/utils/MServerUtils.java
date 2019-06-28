package com.septemberhx.server.utils;

import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.common.utils.MRequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;


@Component
public class MServerUtils {

    private static String MClusterIpAddr;
    private static int MClusterPort;

    @Value("${mserver.mcluster.ip}")
    public void setMClusterIpAddr(String MClusterIpAddr) {
        MServerUtils.MClusterIpAddr = MClusterIpAddr;
    }

    @Value("${mserver.mcluster.port}")
    public void setMClusterPort(Integer MClusterPort) {
        MServerUtils.MClusterPort = MClusterPort;
    }

    private static RestTemplate restTemplate = new RestTemplate();

    public static MInstanceInfoResponse fetchAllInstanceInfo() {
        return MRequestUtils.sendRequest(
                MUrlUtils.getMclusterFetchInstanceInfoUri(),
                null,
                MInstanceInfoResponse.class,
                RequestMethod.GET);
    }

    public static void notifyAddNewRemoteUri(String instanceId, String mObjectId, String funcName) {
        MInstanceRestInfoBean infoBean = new MInstanceRestInfoBean();
        infoBean.setRestAddress("");
        infoBean.setObjectId(mObjectId);
        infoBean.setFunctionName(funcName);
        MSetRestInfoRequest setRestInfoRequest = new MSetRestInfoRequest();
        setRestInfoRequest.setRestInfoBean(infoBean);
        setRestInfoRequest.setInstanceId(instanceId);
        MServerUtils.sendSetRestInfoRequest(setRestInfoRequest);
    }

    public static void notifyDeleteRemoteUri(String instanceId, String mObjectId, String funcName) {
        MInstanceRestInfoBean infoBean = new MInstanceRestInfoBean();
        infoBean.setRestAddress(null);
        infoBean.setObjectId(mObjectId);
        infoBean.setFunctionName(funcName);
        MSetRestInfoRequest setRestInfoRequest = new MSetRestInfoRequest();
        setRestInfoRequest.setRestInfoBean(infoBean);
        setRestInfoRequest.setInstanceId(instanceId);
        MServerUtils.sendSetRestInfoRequest(setRestInfoRequest);
    }

    private static void sendSetRestInfoRequest(MSetRestInfoRequest restInfoRequest) {
        MRequestUtils.sendRequest(
                MUrlUtils.getMClientAgentSetRestInfoUri(MClusterIpAddr, MClusterPort),
                restInfoRequest, null, RequestMethod.POST);
    }
}
