package com.septemberhx.mclient.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;


@FeignClient("MClusterAgent")
public interface MClusterAgentClient {
    @RequestMapping("/magent/remoteuri")
    public URI getRemoteUri(@RequestParam("objectId") String mObjectId, @RequestParam("functionName") String funcName);
}
