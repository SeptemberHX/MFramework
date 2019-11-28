package com.septemberhx.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: septemberhx
 * @Date: 2019-02-22
 * @Version 0.1
 */
@EnableEurekaServer
@SpringBootApplication
@EnableFeignClients
public class EurekaApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplicationMain.class, args);
    }
}
