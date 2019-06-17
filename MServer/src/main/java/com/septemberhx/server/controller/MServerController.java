package com.septemberhx.server.controller;

import com.septemberhx.common.bean.MInstanceInfoBean;
import com.septemberhx.server.core.MSnapshot;
import com.septemberhx.server.core.MSystemModel;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@EnableAutoConfiguration
@RequestMapping("/mserver")
public class MServerController {

    @ResponseBody
    @RequestMapping(path = "/loadInstanceInfo", method = RequestMethod.POST)
    public void loadInstanceInfo(MInstanceInfoBean instanceInfo, HttpServletRequest request) {
        MSystemModel systemModel = MSnapshot.getInstance().getSystemModel();
        String instanceIp = request.getRemoteAddr();
        systemModel.loadInstanceInfo(instanceInfo, instanceIp);
    }
}
