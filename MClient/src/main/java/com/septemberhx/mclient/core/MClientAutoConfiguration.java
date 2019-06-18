package com.septemberhx.mclient.core;

import com.septemberhx.mclient.controller.MClientController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MClientAutoConfiguration {
    @Bean
    public MClientController mClientController() {
        return new MClientController();
    }
}
