package com.septemberhx.server.controller;

import com.septemberhx.common.bean.InstanceInfoBean;
import com.septemberhx.server.core.MSnapshot;
import com.septemberhx.server.core.MSystemModel;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequestMapping("/mserver")
public class MServerController {

    @ResponseBody
    @RequestMapping(path = "/loadInstanceInfo", method = RequestMethod.POST)
    public void loadInstanceInfo(InstanceInfoBean instanceInfo) {
        MSystemModel systemModel = MSnapshot.getInstance().getSystemModel();
        systemModel.loadInstanceInfo(instanceInfo);
    }
}
