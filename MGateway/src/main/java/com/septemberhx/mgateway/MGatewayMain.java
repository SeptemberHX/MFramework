package com.septemberhx.mgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class MGatewayMain {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(MGatewayMain.class, args);
    }
}
