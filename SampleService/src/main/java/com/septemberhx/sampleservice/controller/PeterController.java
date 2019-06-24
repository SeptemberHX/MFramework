package com.septemberhx.sampleservice.controller;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.annotation.MApiType;
import com.septemberhx.mclient.annotation.MFunctionType;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.sampleservice.utils.MCalcUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeterController extends MObject {

    @MFunctionType
    MCalcUtils mCalcUtils;

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
    @RequestMapping(path = "/peterson", method = RequestMethod.GET)
    public String peterson() {
        return this.mCalcUtils.upper("peterson");
    }
}
