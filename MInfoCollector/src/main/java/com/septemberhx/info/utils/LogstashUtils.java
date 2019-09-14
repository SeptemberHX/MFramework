package com.septemberhx.info.utils;

import com.septemberhx.common.utils.MUrlUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogstashUtils {

    /**
     * send the json object to logstash through POST request.
     * @param jsonObjectStr: message you want to send
     */
    public static void sendInfoToLogstash(String logstashIp, int logstashPort, String jsonObjectStr) {
        try {
            send(MUrlUtils.getRemoteUri(logstashIp, logstashPort, "").toString(), jsonObjectStr, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void send(String url, String jsonObjectStr, String encoding) throws IOException {
        String body = "";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity s = new StringEntity(jsonObjectStr, "utf-8");
        s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json"));
        httpPost.setEntity(s);
        httpPost.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = EntityUtils.toString(entity, encoding);
        }

        EntityUtils.consume(entity);
        response.close();
    }
}
