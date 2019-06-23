package com.septemberhx.agent.middleware;

import com.septemberhx.common.bean.MDockerInfoBean;

public interface MDockerManager {
    public MDockerInfoBean getDockerInfoByIpAddr(String ipAddr);
}
