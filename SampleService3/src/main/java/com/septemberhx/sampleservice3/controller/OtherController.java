package com.septemberhx.sampleservice3.controller;

import com.septemberhx.mclient.annotation.MApiFunction;
import com.septemberhx.mclient.annotation.MRestApiType;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.mclient.base.MResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

@RestController
public class OtherController extends MObject {

    @RequestMapping(value = "/age")
    @MRestApiType
    @MApiFunction
    public MResponse wrapper(@RequestParam("rawStr") String rawStr) {
        String a = null;

        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new MResponse().set("result", resultStr);
    }
}
