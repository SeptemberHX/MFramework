package com.septemberhx.agent;

import com.septemberhx.agent.utils.MClientUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AgentApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(AgentApplicationMain.class, args);
//        MClientUtils.readPodYaml("sampleservice");
    }
}
