package com.jerry.lab.nio;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessMonitor {

    static Map<Integer, Long> useTime = new ConcurrentHashMap<>();

    public static void serverReceived(int order) {
        System.out.println("[Server][" + order + "][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "][Received]");
    }

    public static void serverReturn(int order) {
        System.out.println("[Server][" + order + "][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "][Return]");
    }

    public static void clientSend(int order) {
        //useTime.put(order, System.currentTimeMillis());
        System.out.println("[Client][" + order + "][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "][Send]");
    }

    public static void clientReceived(int order) {
        //useTime.put(order, System.currentTimeMillis() - useTime.get(order));
        System.out.println("[Client][" + order + "][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "][Received]");
    }

    /*public static void displayUseTime() {
        useTime.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        });
        System.out.println("avg time -> " + useTime.values().stream().collect(Collectors.averagingLong(Long :: valueOf)));

    }*/
}
