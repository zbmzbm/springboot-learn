package com.example.springbootgray.apiversion;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author zbm
 * @date 2024/3/811:00
 */
@RefreshScope
@Data
@ConfigurationProperties(prefix = "api.version")
public class ApiVersionProperties {

    boolean enabled;
}
