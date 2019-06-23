package com.septemberhx.agent.utils;

import com.septemberhx.agent.middleware.MDockerManager;
import com.septemberhx.agent.middleware.MDockerManagerK8SImpl;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MDockerInfoBean;
import org.springframework.web.client.RestTemplate;


public class MClientUtils {

    private static final String MCLIENTPORT = "8081";
    private static RestTemplate restTemplate = new RestTemplate();

    private static MDockerManager dockerManager = new MDockerManagerK8SImpl();

    public static MClientInfoBean getMClientInfo(String serverIp) {
        MClientInfoBean result = null;
        MDockerInfoBean dockerInfoBean = null;
        try {
            result = restTemplate.getForObject("http://" + serverIp + ":" + MCLIENTPORT +  "/mclient/info",
                    MClientInfoBean.class);
            dockerInfoBean = dockerManager.getDockerInfoByIpAddr(serverIp);
            result.setDockerInfoBean(dockerInfoBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
