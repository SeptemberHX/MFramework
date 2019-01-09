package test;

import base.MObject;
import http.MHttpProxy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: septemberhx
 * @Date: 2018-12-24
 * @Version 0.1
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/MFunctionAdd")
public class MHttpMainController extends MObject {

    private MHttpProxy mHttpProxy;

    @Autowired
    private HttpServletRequest request;

    public MHttpMainController() {
        this.mHttpProxy = new MHttpProxy(MFunctionAdd.class);
    }

    @ResponseBody
    @RequestMapping(path = "/*", method = RequestMethod.POST)
    public String execute(@RequestBody String jsonStr) throws HttpRequestMethodNotSupportedException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        String[] splitResult = request.getRequestURI().split("/");
        System.out.println(jsonObject);

        String result = null;
        if (splitResult.length > 0) {
            result = this.mHttpProxy.execute(splitResult[splitResult.length - 1], jsonObject);
        }

        if (result == null) {
            throw new HttpRequestMethodNotSupportedException(splitResult[splitResult.length - 1]);
        } else {
            JSONObject resultJsonObject = new JSONObject();
            resultJsonObject.put("result", result);
            return resultJsonObject.toString();
        }
    }
}
