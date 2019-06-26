package com.septemberhx.sampleservice;

import com.septemberhx.mclient.annotation.MClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MClient
@EnableEurekaClient
@EnableFeignClients
public class SampleServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(SampleServiceMain.class, args);
    }
}
