package com.septemberhx.server.controller;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.common.bean.MInstanceInfoResponse;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.bean.MSetRestInfoRequest;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.core.MSnapshot;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MServerUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RestController
@EnableAutoConfiguration
@RequestMapping("/mserver")
public class MServerController {

    @ResponseBody
    @RequestMapping(path = "/loadInstanceInfo", method = RequestMethod.POST)
    public void loadInstanceInfo(MInstanceInfoBean instanceInfo, HttpServletRequest request) {
        MSystemModel systemModel = MSnapshot.getInstance().getSystemModel();
        systemModel.loadInstanceInfo(instanceInfo);
    }

    @ResponseBody
    @RequestMapping(path = "/getInstanceInfos", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfos() {
        MInstanceInfoResponse response = MServerUtils.fetchAllInstanceInfo();
        for (MInstanceInfoBean infoBean : response.getInfoBeanList()) {
            MServerSkeleton.getInstance().updateInstanceInfo(infoBean);
        }
        return MServerUtils.fetchAllInstanceInfo();
    }

    @ResponseBody
    @RequestMapping(path = "/remoteuri", method = RequestMethod.GET)
    public URI getRemoteUri(@RequestParam("objectId") String mObjectId, @RequestParam("functionName") String funcName) {
        return MServerSkeleton.getInstance().getRemoteUri(mObjectId, funcName);
    }

    @ResponseBody
    @RequestMapping(path = "/setRemoteUri", method = RequestMethod.POST)
    public void setRemoteUri(@RequestBody MSetRestInfoRequest restInfoRequest) {
        MServerSkeleton.getInstance().setRemoteUri(
                restInfoRequest.getInstanceId(),
                restInfoRequest.getRestInfoBean().getObjectId(),
                restInfoRequest.getRestInfoBean().getFunctionName(),
                restInfoRequest.getRestInfoBean().getRestAddress()
        );
    }
}
