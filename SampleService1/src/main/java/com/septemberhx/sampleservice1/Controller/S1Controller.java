package com.septemberhx.sampleservice1.Controller;

import com.septemberhx.sampleservice2.controller.S2Controller;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
public class S1Controller {

    S2Controller s2Controller = new S2Controller();

    @RequestMapping("/wrapper")
    public JSONObject wrapper(@RequestParam("rawStr") String rawStr) {
        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", resultStr);
        return jsonObject;
    }

    public JSONObject composition(@RequestParam("rawStr") String rawStr) {
        JSONObject s1 = this.wrapper(rawStr);
//        JSONObject s2 = this.s2Controller.wrapper(s1);
        return s1;
    }

    // should be auto-generated
    public JSONObject wrapper(JSONObject parameters) {
        String rawStr = (String)parameters.get("rawStr");

        String resultStr = rawStr;
        try {
            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", resultStr);
        return jsonObject;
    }
}
