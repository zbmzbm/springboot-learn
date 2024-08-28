package com.example.springbootgray.gray;

import org.springframework.cloud.client.ServiceInstance;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class SmoothWeightedRoundRobin {
    private volatile List<Node> nodeList = new ArrayList<>() ; // 保存权重
    private ReentrantLock lock = new ReentrantLock() ;

    public SmoothWeightedRoundRobin(Node ...nodes) {
        for (Node node : nodes) {
            nodeList.add(node) ;
        }
    }

    public SmoothWeightedRoundRobin(Map<List<ServiceInstance>,Integer> nodes) {
        for (Map.Entry<List<ServiceInstance>, Integer> entry : nodes.entrySet()) {
            nodeList.add(new Node(entry.getKey(), entry.getValue())) ;
        }
    }

    public Node select(){
        try {
            lock.lock();
            return this.selectInner() ;
        }finally {
            lock.unlock();
        }
    }

    private Node selectInner(){
        int totalWeight = 0 ;
        Node maxNode = null ;
        int maxWeight = 0 ;

        for (int i = 0; i < nodeList.size(); i++) {
            Node n = nodeList.get(i);
            totalWeight += n.getWeight() ;

            // 每个节点的当前权重要加上原始的权重
            n.setCurrentWeight(n.getCurrentWeight() + n.getWeight());

            // 保存当前权重最大的节点
            if (maxNode == null || maxWeight < n.getCurrentWeight() ) {
                maxNode = n ;
                maxWeight = n.getCurrentWeight() ;
            }
        }
        // 被选中的节点权重减掉总权重
        maxNode.setCurrentWeight(maxNode.getCurrentWeight() - totalWeight);

//        nodeList.forEach(node -> System.out.print(node.getCurrentWeight()));
        return maxNode ;
    }


    public static void main(String[] args) {
        /**
         * 假设有三个服务器权重配置如下：
         * server A  weight = 4 ;
         * server B  weight = 3 ;
         * server C  weight = 2 ;
         */
        List<ServiceInstance> serverList1 = new ArrayList<>() ;
        List<ServiceInstance> serverList2 = new ArrayList<>() ;
        Node serverA = new Node(serverList1, 7);
        Node serverB = new Node(serverList2, 3);

        SmoothWeightedRoundRobin smoothWeightedRoundRobin = new SmoothWeightedRoundRobin(serverA,serverB);
        for (int i = 0; i < 10; i++) {
            Node i1 = smoothWeightedRoundRobin.select();
            System.out.println(i1.getServerName());
        }


    }
}
