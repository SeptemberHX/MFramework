package com.septemberhx.common.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class RequestUtils {

    private static RestTemplate restTemplate = new RestTemplate();

    public static <T> T sendRequest(URI uri, @Nullable Object paramObj, Class<T> returnClass, RequestMethod method) {
        T result = null;
        ResponseEntity<T> entity = null;
        try {
            switch (method) {
                case GET:
                    entity = RequestUtils.restTemplate.getForEntity(uri, returnClass);
                    break;
                case POST:
                    entity = RequestUtils.restTemplate.postForEntity(uri, paramObj, returnClass);
                    break;
                default:
                    break;
            }
            result = entity.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
