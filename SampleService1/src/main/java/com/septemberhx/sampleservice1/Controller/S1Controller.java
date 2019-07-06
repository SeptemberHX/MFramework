package com.septemberhx.sampleservice1.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
public class S1Controller {
    @RequestMapping("/wrapper")
    public String wrapper(@RequestParam("rawStr") String rawStr) {
        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }
}
