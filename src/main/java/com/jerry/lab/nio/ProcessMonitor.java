package com.jerry.lab.nio;

import org.omg.CORBA.StringHolder;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ProcessMonitor {

    static Map<String, Long> useTime = new ConcurrentHashMap<>();

    public static void serverReceived(String param) {
        System.out.println("[Server][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "] Received : " + param);
    }

    public static void serverReturn(String param) {
        System.out.println("[Server][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "] Return : " + param);
    }

    public static void clientSend(String param) {
        useTime.put(param, System.currentTimeMillis());
        System.out.println("[Client][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "] Send : " + param);
    }

    public static void clientReceived(String param) {
        useTime.put(param, System.currentTimeMillis() - useTime.get(param));
        System.out.println("[Client][" + Thread.currentThread().getId() + "][" + LocalTime.now() + "] Received : " + param);
    }

    public static void displayUseTime() {
        useTime.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        });
        System.out.println("avg time -> " + useTime.values().stream().collect(Collectors.averagingLong(Long :: valueOf)));

    }
}
