package com.septemberhx.sampleservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;


@FeignClient(value = "MClusterAgent1")
public interface MClusterAgentClient {
    @RequestMapping(value = "/magent/remoteuri", method = RequestMethod.POST)
    URI getRemoteUri(@RequestParam("objectId") String mObjectId, @RequestParam("functionName") String funcName);
}
