package com.septemberhx.test;

import mclient.annotation.MApiType;
import mclient.annotation.MFunctionType;
import mclient.annotation.MServiceType;
import mclient.base.MObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: septemberhx
 * @Date: 2018-12-24
 * @Version 0.2
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/MFunctionAdd")
@MServiceType
public class MHttpMainController extends MObject {

    @MFunctionType
    private MFunctionAdd functionAdd;

    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public String execute(@RequestBody String jsonStr) throws HttpRequestMethodNotSupportedException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        System.out.println(jsonObject);

        int r = this.functionAdd.addInt(jsonObject.getInt("x"), jsonObject.getInt("y"));

        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("result", r);
        return resultJsonObject.toString();
    }

    @MApiType
    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello, world!";
    }
}
