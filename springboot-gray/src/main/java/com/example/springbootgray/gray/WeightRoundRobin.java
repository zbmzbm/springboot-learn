package com.example.springbootgray.gray;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zbm
 * @date 2024/4/2815:45
 */
public class WeightRoundRobin {
    @Data
    public static class Server {
        private String serverId;
        private String instanceId;
        private int weight;
        private int currentWeight;

        public Server(String serverId, String instanceId) {
            this.serverId = serverId;
            this.instanceId = instanceId;
        }

        public Server(String serverId, String instanceId, int weight) {
            this.serverId = serverId;
            this.instanceId = instanceId;
            this.weight = weight;
        }

        public void selected(int total) {
            this.currentWeight -= total;
        }

        public void incrCurrentWeight() {
            this.currentWeight += weight;
        }
    }

    public static Server selectServer(List<Server> serverList) {
        int total = 0;
        Server selectedServer = null;
        int maxWeight = 0;
        for (Server server : serverList) {
            total += server.getWeight();
            server.incrCurrentWeight();
            //选取当前权重最大的一个服务器
            if (selectedServer == null || maxWeight < server.getCurrentWeight()) {
                selectedServer = server;
                maxWeight = server.getCurrentWeight();
            }
        }
        if (selectedServer == null) {
            Random random = new Random();
            int next = random.nextInt(serverList.size());
            return serverList.get(next);
        }
        selectedServer.selected(total);
        return selectedServer;
    }
}
