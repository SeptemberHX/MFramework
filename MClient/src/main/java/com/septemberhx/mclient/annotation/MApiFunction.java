package com.septemberhx.mclient.annotation;

import com.septemberhx.mclient.core.MClientAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MClientAutoConfiguration.class)
public @interface MApiFunction {
}
