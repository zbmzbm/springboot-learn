package com.example.springbootgray.gray;

import lombok.Data;

import java.util.Map;

/**
 * @author zbm
 * @date 2024/3/2218:19
 */
@Data
public class Server {

    private int serverId;
    private String name;
    private int weight;
    private int currentWeight;
    public Server(int serverId, String name) {
        this.serverId = serverId;
        this.name = name;
    }
    public Server(int serverId, String name, int weight) {
        this.serverId = serverId;
        this.name = name;
        this.weight = weight;
    }
    public void selected(int total) {
        this.currentWeight -= total;
    }
    public void incrCurrentWeight() {
        this.currentWeight += weight;
    }
}
