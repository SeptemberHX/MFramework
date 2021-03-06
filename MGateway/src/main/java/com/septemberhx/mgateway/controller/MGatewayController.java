package com.septemberhx.mgateway.controller;

import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUpdateCacheBean;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.bean.MAllUserBean;
import com.septemberhx.common.log.MFunctionCallEndLog;
import com.septemberhx.common.log.MFunctionCalledLog;
import com.septemberhx.common.utils.MLogUtils;
import com.septemberhx.common.bean.MUserRequestBean;
import com.septemberhx.mgateway.MGatewayMain;
import com.septemberhx.mgateway.bean.MUserListBean;
import com.septemberhx.mgateway.client.MClusterAgentClient;
import com.septemberhx.mgateway.core.MGatewayCache;
import com.septemberhx.mgateway.core.MGatewayProcess;
import com.septemberhx.mgateway.utils.MGatewayUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
@RestController
public class MGatewayController {

    @Autowired
    private MClusterAgentClient clusterAgentClient;

    @PostConstruct
    public void init() {
        MGatewayUtils.clusterAgentClient = clusterAgentClient;
    }

    @ResponseBody
    @RequestMapping(path = "/allUser", method = RequestMethod.POST)
    public MAllUserBean getAllUser() {
        return new MAllUserBean(MGatewayCache.getInstance().getAllUser());
    }

    @ResponseBody
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void registerUser(@RequestBody MUser user) {
        MGatewayCache.getInstance().addUser(user);
    }

    @ResponseBody
    @RequestMapping(path = "/registerUserList", method = RequestMethod.POST)
    public void registerUsers(@RequestBody MUserListBean userListBean) {
        for (MUser user : userListBean.getUserList()) {
            MGatewayCache.getInstance().addUser(user);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/reset", method = RequestMethod.GET)
    public void clearCache() {
        MGatewayCache.getInstance().clear();
    }

    @ResponseBody
    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public MResponse doRequest(@RequestBody MUserRequestBean requestBean, HttpServletRequest request) {
        MFunctionCalledLog callLog = new MFunctionCalledLog();
        callLog.setLogDateTime(DateTime.now());
        callLog.setLogFromIpAddr(request.getRemoteAddr());
        callLog.setLogFromPort(request.getRemotePort());
        callLog.setLogIpAddr(request.getLocalAddr());
        callLog.setLogUserId(requestBean.getUserDemand().getUserId());
        callLog.setLogMethodName(requestBean.getUserDemand().getId());
        callLog.setLogObjectId("MGateway");
        MLogUtils.log(callLog);
        MResponse response = MGatewayProcess.doRequest(requestBean);

        MFunctionCallEndLog endLog = new MFunctionCallEndLog();
        endLog.setLogDateTime(DateTime.now());
        endLog.setLogFromIpAddr(request.getRemoteAddr());
        endLog.setLogFromPort(request.getRemotePort());
        endLog.setLogIpAddr(request.getLocalAddr());
        endLog.setLogUserId(requestBean.getUserDemand().getUserId());
        endLog.setLogMethodName(requestBean.getUserDemand().getId());
        endLog.setLogObjectId("MGateway");
        MLogUtils.log(endLog);
        return response;
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
