package com.jerry.lab.nio;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessMonitor {

    private static long begin;//seconds
    private static List<MonitorNode> monitorNodeList = Collections.synchronizedList(new ArrayList<>());

    public static void begin(){
        begin = Instant.now().getEpochSecond();
    }

    public static void serverReceived(byte order) {
        monitorNodeList.add(new MonitorNode((Instant.now().getEpochSecond() - begin), order, 2));
        System.out.println("[Server][" + order + "][" + Thread.currentThread().getId() + "][" + (Instant.now().getEpochSecond() - begin) + "][Received]");
    }

    public static void serverReturn(byte order) {
        monitorNodeList.add(new MonitorNode((Instant.now().getEpochSecond() - begin), order, 3));
        System.out.println("[Server][" + order + "][" + Thread.currentThread().getId() + "][" + (Instant.now().getEpochSecond() - begin) + "][Return]");
    }

    public static void clientSend(byte order) {
        System.out.println(monitorNodeList);
        monitorNodeList.add(new MonitorNode((Instant.now().getEpochSecond() - begin), order, 1));
        System.out.println(monitorNodeList);
        System.out.println("[Client][" + order + "][" + Thread.currentThread().getId() + "][" + (Instant.now().getEpochSecond() - begin) + "][Send]");
    }

    public static void clientReceived(byte order) {
        monitorNodeList.add(new MonitorNode((Instant.now().getEpochSecond() - begin), order, 4));
        System.out.println("[Client][" + order + "][" + Thread.currentThread().getId() + "][" + (Instant.now().getEpochSecond() - begin) + "][Received]");
    }

    public static void clientProcessed(byte order) {
        monitorNodeList.add(new MonitorNode((Instant.now().getEpochSecond() - begin), order, 5));
        System.out.println("[Client][" + order + "][" + Thread.currentThread().getId() + "][" + (Instant.now().getEpochSecond() - begin) + "][Process]");
    }

    public static void displayProcess() {
        monitorNodeList.stream().forEach(monitorNode -> {
            System.out.println(monitorNode);
        });;
        /*monitorNodeList.stream().sorted(Comparator.comparing(monitor -> {
            System.out.println(monitor.getSecond());
            return monitor.getSecond();
        }))
                .collect(Collectors.toList())
                .forEach(monitorNode -> {
                    System.out.println(monitorNode);
                });*/
    }

    @Data
    static class MonitorNode {
        long second;
        byte order;

        /**
         * 1 Client Send
         * 2 Server Received
         * 3 Server Return
         * 4 Client Received
         * 5 Client Processed
         */
        int step;

        public MonitorNode(long second, byte order, int step) {
            this.second = second;
            this.order = order;
            this.step = step;
        }
    }
}
