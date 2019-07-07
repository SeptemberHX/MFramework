package com.septemberhx.sampleservice1.Controller;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.annotation.MFunctionType;
import com.septemberhx.mclient.annotation.MRestApiType;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.mclient.base.MResponse;
import com.septemberhx.sampleservice2.controller.S2Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
public class S1Controller extends MObject {

    @MFunctionType
    S2Controller s2Controller;

    @RequestMapping("/wrapper")
    @MRestApiType
    @MApiFunction
    public MResponse wrapper(@RequestParam("rawStr") String rawStr) {
        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new MResponse().set("rawStr", resultStr);
    }

    @RequestMapping("/s1_s2")
    @ResponseBody
    public MResponse composition(@RequestParam("rawStr") String rawStr) {
        MResponse s1 = this.wrapper(rawStr);
        MResponse s2 = this.s2Controller.wrapper(s1);
        return s2;
    }
}
