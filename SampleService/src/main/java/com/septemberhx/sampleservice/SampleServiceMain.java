package com.septemberhx.sampleservice;

import com.septemberhx.mclient.annotation.MClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MClient
public class SampleServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(SampleServiceMain.class, args);
    }
}
