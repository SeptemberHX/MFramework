package com.septemberhx.mclient.annotation;

import com.septemberhx.mclient.core.MClientAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MClientAutoConfiguration.class)
@EnableFeignClients
public @interface MClient {
}
