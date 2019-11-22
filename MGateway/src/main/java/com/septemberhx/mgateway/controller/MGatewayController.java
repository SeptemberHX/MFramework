package com.septemberhx.mgateway.controller;

import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUpdateCacheBean;
import com.septemberhx.mgateway.bean.MUserRequestBean;
import com.septemberhx.mgateway.core.MGatewayCache;
import com.septemberhx.mgateway.core.MGatewayProcess;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
@RestController
public class MGatewayController {

    @ResponseBody
    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public MResponse doRequest(@RequestBody MUserRequestBean requestBean) {
        return MGatewayProcess.doRequest(requestBean.getUserDemand(), requestBean.getData());
    }

    @ResponseBody
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public void updateCache(@RequestBody MUpdateCacheBean cacheBean) {
        Map<String, String> newMap = cacheBean.getDemandId2Url();
        for (String demandId : newMap.keySet()) {
            MGatewayCache.getInstance().update(demandId, newMap.get(demandId));
        }
    }
}
