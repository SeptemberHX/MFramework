package com.septemberhx.eureka;

import com.netflix.appinfo.InstanceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "MCLUSTERAGENT")
public interface MClusterAgentClient {
    @RequestMapping(value = "/magent/registerd", method = RequestMethod.POST)
    void instanceRegistered(@RequestBody InstanceInfo instanceInfo);
}
