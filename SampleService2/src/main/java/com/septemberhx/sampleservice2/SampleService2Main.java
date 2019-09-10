package com.septemberhx.sampleservice2;

import com.septemberhx.mclient.annotation.MClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@MClient
@EnableEurekaClient
public class SampleService2Main {
    public static void main(String[] args) {
        SpringApplication.run(SampleService2Main.class, args);
    }
}
