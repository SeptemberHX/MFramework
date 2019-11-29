package com.septemberhx.mgateway.client;

import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.bean.MInstanceRegisterNotifyRequest;
import com.septemberhx.common.bean.MUserRequestBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = MClusterConfig.MCLUSTERAGENT_NAME)
public interface MClusterAgentClient {
    @RequestMapping(value = MClusterConfig.MCLUSTERAGNET_FETCH_REQUEST_URL, method = RequestMethod.POST)
    String fetchRequestUrl(@RequestBody MUserDemand userDemand);

    @RequestMapping(path = MClusterConfig.MCLUSTERAGENT_DO_REQUEST_URL, method = RequestMethod.POST)
    MResponse doRequest(@RequestBody MUserRequestBean requestBean);
}
