package com.septemberhx.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MRequestUtils {

    private static RestTemplate restTemplate = new RestTemplate();
    private static Logger logger = LogManager.getLogger(MRequestUtils.class);

    public static <T> T sendRequest(URI uri, @Nullable Object paramObj, Class<T> returnClass, RequestMethod method) {
        return sendRequest(uri, paramObj, returnClass, method, new HashMap<>());
    }

    public static <T> T sendRequest(URI uri, @Nullable Object paramObj, Class<T> returnClass, RequestMethod method, Map<String, List<String>> customHeaders) {
        T result = null;
        ResponseEntity<T> entity = null;
        try {
            switch (method) {
                case GET:
                    if (paramObj != null) {
                        Map<String, String> paraMap = (Map) paramObj;
                        uri = MUrlUtils.getRemoteUriWithQueries(uri, paraMap);
                    }
                    entity = MRequestUtils.restTemplate.getForEntity(uri, returnClass);
                    break;
                case POST:
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    for (String key : customHeaders.keySet()) {
                        headers.put(key, customHeaders.get(key));
                    }
                    HttpEntity<T> param = new HttpEntity<T>((T)paramObj, headers);
                    entity = MRequestUtils.restTemplate.postForEntity(uri, param, returnClass);
                    break;
                default:
                    break;
            }
            result = entity.getBody();
        } catch (Exception e) {
            logger.warn(String.format("Failed to send request to %s in %s", uri, method));
        }
        return result;
    }
}
