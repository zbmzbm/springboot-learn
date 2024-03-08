package com.example.springbootgray.apiversion;

import java.lang.annotation.*;

/**
 * @author zbm
 * @date 2024/3/810:50
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    /**
     * 版本号
     * @return
     */
    int value() default 1;
}