package com.example.springbootgray.apiversion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @Author: zbm
 * @Date: 2024年03月08日10:55:45
 */
@Configuration
@EnableConfigurationProperties(ApiVersionProperties.class)
@ConditionalOnProperty(
        value = {"api.version.enabled"},
        matchIfMissing = true
)
public class WebConfig implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiRequestHandlerMapping();
    }
}
