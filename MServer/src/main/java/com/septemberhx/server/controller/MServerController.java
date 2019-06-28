package com.septemberhx.server.controller;

import com.septemberhx.common.bean.*;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.core.MSnapshot;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MServerUtils;
import org.springframework.beans.factory.annotation.Value;
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
    @RequestMapping(path = "/remoteuri", method = RequestMethod.POST)
    public URI getRemoteUri(@RequestBody MGetRemoteUriRequest remoteUriRequest) {
        return MServerSkeleton.getInstance().getRemoteUri(remoteUriRequest);
    }

    @ResponseBody
    @RequestMapping(path = "/setRemoteUri", method = RequestMethod.POST)
    public void setRemoteUri(@RequestBody MSetRestInfoRequest restInfoRequest) {
        MServerSkeleton.getInstance().setRemoteUri(restInfoRequest);
    }
}
