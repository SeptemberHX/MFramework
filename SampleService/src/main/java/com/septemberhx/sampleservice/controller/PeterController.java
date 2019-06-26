package com.septemberhx.sampleservice.controller;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.annotation.MApiType;
import com.septemberhx.mclient.annotation.MFunctionType;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.sampleservice.client.MClusterAgentClient;
import com.septemberhx.sampleservice.utils.MCalcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sample")
public class PeterController extends MObject {

    @MFunctionType
    MCalcUtils mCalcUtils;

    @Autowired
    MClusterAgentClient agentClient;

    @ResponseBody
    @MApiType
    @MApiFunction
    @RequestMapping(path = "/peter", method = RequestMethod.GET)
    public String peter() {
        return this.mCalcUtils.wrapper("peter");
    }

    @ResponseBody
    @MApiType
    @MApiFunction
    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam("name") String name, @RequestParam("age") Integer age) {
        return "Hello " + name + ", age " + age;
    }


    @ResponseBody
    @MApiType
    @MApiFunction
    @RequestMapping(path = "/peterson", method = RequestMethod.GET)
    public String peterson() {
        agentClient.getRemoteUri("test", "test");
        return this.mCalcUtils.upper("peterson");
    }
}
