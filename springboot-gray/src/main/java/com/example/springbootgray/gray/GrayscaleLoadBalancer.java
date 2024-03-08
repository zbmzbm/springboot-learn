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
import java.util.List;
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
        // 指定ip则返回满足ip的服务
        List<String> priorIpPattern = LoadBalancerProperties.getPriorIpPattern();
        if (!priorIpPattern.isEmpty()) {
            String[] priorIpPatterns = priorIpPattern.toArray(new String[0]);
            List<ServiceInstance> priorIpInstances = instances.stream().filter(
                    (i -> PatternMatchUtils.simpleMatch(priorIpPatterns, i.getHost()))
            ).collect(Collectors.toList());
            if (!priorIpInstances.isEmpty()) {
                instances = priorIpInstances;
            }
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


    public Response<ServiceInstance> selectFromSets(List<ServiceInstance> instances, List<ServiceInstance> greyInstances) {
        Random rand = new Random();
        // 生成 0 到 9 之间的随机数，用于按照 80% 和 20% 的比例确定从哪个集合取数
        int randomValue = rand.nextInt(100);
        if(LoadBalancerProperties.getWeight() !=null){
            if (randomValue < LoadBalancerProperties.getWeight()) {
                if (!greyInstances.isEmpty()) {
                    int index = rand.nextInt(greyInstances.size());
                    return new DefaultResponse(greyInstances.get(index));
                }
            } else {
                if (!instances.isEmpty()) {
                    int index = rand.nextInt(instances.size());
                    return new DefaultResponse(instances.get(index));
                }
            }
        }
        // 如果集合 A 和集合 B 都为空，则返回A
        // 挑选随机节点返回
        int randomIndex = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance instance = instances.get(randomIndex % instances.size());
        return new DefaultResponse(instance);
    }

}

