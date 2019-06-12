package test;

import annotation.MFunctionType;
import base.MObject;
import core.MClient;
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
}
