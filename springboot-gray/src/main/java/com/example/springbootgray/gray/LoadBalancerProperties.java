package com.example.springbootgray.gray;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zbm
 * @date 2024/3/811:37
 */
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(LoadBalancerProperties.PROPERTIES_PREFIX)
public class LoadBalancerProperties {
    public static final String PROPERTIES_PREFIX = "gray.loadbalancer";

    /**
     * 是否开启自定义负载均衡
     */
    private boolean enabled;
    /**
     * 灰度服务版本
     */
    private String version;

    /**
     * 权重比例,百分多少从灰度版本取
     */
    private Integer weight;
}
