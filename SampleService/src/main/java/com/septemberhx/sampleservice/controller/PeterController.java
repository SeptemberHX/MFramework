package com.septemberhx.sampleservice.controller;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.annotation.MApiType;
import com.septemberhx.mclient.base.MObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeterController extends MObject {

    @ResponseBody
    @MApiType
    @MApiFunction
    @RequestMapping(path = "/peter", method = RequestMethod.GET)
    public String peter() {
        return "Peter";
    }

    @ResponseBody
    @MApiType
    @MApiFunction
    @RequestMapping(path = "/peterson", method = RequestMethod.GET)
    public String peterson() {
        return "Peterson";
    }
}
