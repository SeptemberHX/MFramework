package com.septemberhx.common.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class MRequestUtils {

    private static RestTemplate restTemplate = new RestTemplate();

    public static <T> T sendRequest(URI uri, @Nullable Object paramObj, Class<T> returnClass, RequestMethod method) {
        T result = null;
        ResponseEntity<T> entity = null;
        try {
            switch (method) {
                case GET:
                    entity = MRequestUtils.restTemplate.getForEntity(uri, returnClass);
                    break;
                case POST:
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<T> param = new HttpEntity<T>((T)paramObj, headers);
                    entity = MRequestUtils.restTemplate.postForEntity(uri, param, returnClass);
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
