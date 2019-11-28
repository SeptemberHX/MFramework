package com.septemberhx.eureka.client;

import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MInstanceRegisterNotifyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = MClusterConfig.MCLUSTERAGENT_NAME)
public interface MClusterAgentClient {
    @RequestMapping(value = MClusterConfig.MCLUSTERAGENT_INSTANCE_REGISTER_URL, method = RequestMethod.POST)
    void instanceRegistered(@RequestBody MInstanceRegisterNotifyRequest registerNotifyRequest);
}
