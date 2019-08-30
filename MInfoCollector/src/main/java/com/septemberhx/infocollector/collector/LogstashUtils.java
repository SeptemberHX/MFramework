package com.septemberhx.infocollector.collector;

import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogstashUtils {

    private static String LOGSTASH_IP = "192.168.1.102";
    private static Integer LOGSTASH_PORT = 4040;

    /**
     * send the json object to logstash through POST request.
     * @param jsonObject: message you want to send
     */
    public static void sendInfoToLogstash(JSONObject jsonObject) {
        MRequestUtils.sendRequest(
                MUrlUtils.getRemoteUri(LOGSTASH_IP, LOGSTASH_PORT, ""),
                jsonObject, null, RequestMethod.POST);
    }
}
