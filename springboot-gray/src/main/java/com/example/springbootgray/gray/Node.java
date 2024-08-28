package com.example.springbootgray.gray;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public class Node {
    private final int weight ;  // 初始权重 （保持不变）
    private final List<ServiceInstance> serverName ; // 服务名

    private int currentWeight ; // 当前权重

    public Node( List<ServiceInstance> serverName, int weight) {
        this.weight = weight;
        this.serverName = serverName ;
        this.currentWeight = weight ;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public int getWeight() {
        return weight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public List<ServiceInstance> getServerName() {
        return serverName;
    }
}
