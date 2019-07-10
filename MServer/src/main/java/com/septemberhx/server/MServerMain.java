package com.septemberhx.server;


import com.septemberhx.server.utils.MServerUtils;
import io.kubernetes.client.models.V1Pod;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MServerMain {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(MServerMain.class, args);
    }
}
