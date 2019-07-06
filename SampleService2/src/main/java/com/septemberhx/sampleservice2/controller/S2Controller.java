package com.septemberhx.sampleservice2.controller;

import com.septemberhx.mclient.annotation.MRestApiType;
import com.septemberhx.mclient.base.MObject;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
public class S2Controller extends MObject {

    @RequestMapping("/age")
    @MRestApiType
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

    // should be auto-generated
//    public JSONObject wrapper(JSONObject jsonParameters) {
//        String rawStr = (String)jsonParameters.<java.lang.String>get("rawStr");
//        String resultStr = rawStr;
//        try {
//            resultStr += "_" + InetAddress.getLocalHost().getHostAddress();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("result", resultStr);
//        return jsonObject;
//    }
}
