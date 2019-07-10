package com.septemberhx.sampleservice1;

import com.septemberhx.mclient.annotation.MClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MClient
@EnableEurekaClient
public class SampleService1Main {
    public static void main(String[] args) {
        SpringApplication.run(SampleService1Main.class, args);
    }
}
