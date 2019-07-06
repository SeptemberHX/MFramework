package com.septemberhx.sampleservice2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
public class S2Controller {

    @RequestMapping("/age")
    public String wrapper(@RequestParam("rawStr") String rawStr) {
        String resultStr = rawStr;
        try {
            resultStr += "_10_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }
}
