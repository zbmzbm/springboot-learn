package com.example.springbootgray.gray;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author zbm
 * @date 2024/3/811:42
 */
@Slf4j
@RequiredArgsConstructor
public class GrayscaleLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final LoadBalancerProperties LoadBalancerProperties;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    /**
     * 自定义节点规则返回目标节点
     */
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        // 注册中心无可用实例 返回空
        if (CollectionUtils.isEmpty(instances)) {
            return new EmptyResponse();
        }
        // 获取灰度版本号
        DefaultRequestContext context = (DefaultRequestContext) request.getContext();
        RequestData requestData = (RequestData) context.getClientRequest();
        HttpHeaders headers = requestData.getHeaders();
        String versionName = headers.getFirst(LoadBalancerConstant.VERSION_NAME);

        // 没有指定灰度版本则返回正式的服务
        List<ServiceInstance> noneGrayscaleInstances= instances.stream().filter(
                    i -> !i.getMetadata().containsKey(LoadBalancerConstant.VERSION_NAME)
            ).collect(Collectors.toList());

        // 指定灰度版本则返回标记的服务
        List<ServiceInstance> grayscaleInstances=null;
        if (versionName!= null){
            grayscaleInstances= instances.stream().filter(i -> {
                String versionNameInMetadata = i.getMetadata().get(LoadBalancerConstant.VERSION_NAME);
                return versionName.equalsIgnoreCase(versionNameInMetadata);
            }).collect(Collectors.toList());
        }

        return selectFromSets(noneGrayscaleInstances,grayscaleInstances);
    }

    /**
     * 采用随机规则返回
     */
    private Response<ServiceInstance> randomInstance(List<ServiceInstance> instances) {
        // 若没有可用节点则返回空
        if (instances.isEmpty()) {
            return new EmptyResponse();
        }

        // 挑选随机节点返回
        int randomIndex = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance instance = instances.get(randomIndex % instances.size());
        return new DefaultResponse(instance);
    }

    static SmoothWeightedRoundRobin smoothWeightedRoundRobin;

    public Response<ServiceInstance> selectFromSets(List<ServiceInstance> instances, List<ServiceInstance> greyInstances) {
        if (smoothWeightedRoundRobin == null) {
            Integer weight = LoadBalancerProperties.getWeight();
            Map<List<ServiceInstance>, Integer> nodes =new HashMap<>();
            nodes.put(greyInstances,weight);
            nodes.put(instances,100-weight);
            smoothWeightedRoundRobin= new SmoothWeightedRoundRobin(nodes);
        }
        Node node = smoothWeightedRoundRobin.select();
        List<ServiceInstance> serverName = node.getServerName();
        Random random = new Random();
        int randomIndex = random.nextInt(serverName.size());
        return new DefaultResponse(serverName.get(randomIndex));
    }

}

