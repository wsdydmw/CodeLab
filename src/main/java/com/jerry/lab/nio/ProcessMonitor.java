package com.jerry.lab.nio;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * step数字意义
 * 1:CLIENT_SEND
 * 2:SERVER_RECEIVED
 * 3:SERVER_RETURN
 * 4:CLIENT_RECEIVED
 * 5:CLIENT_PROCESSED
 * <p>
 * 1 -- networdDelay --> 2 -- serverProcessDelay --> 3 -- networdDelay --> 4 -- clientProcessDelay --> 5
 */
public class ProcessMonitor {

    private static long begin;//seconds
    private static List<MonitorNode> monitorNodeList = Collections.synchronizedList(new ArrayList<>());

    public static void begin() {
        begin = Instant.now().getEpochSecond();
        System.out.println("[Order][ThreadID][Second][Process]");
    }

    public static void clientSend(byte order) {
        MonitorNode monitorNode = new MonitorNode(order, 1);
        monitorNodeList.add(monitorNode);
        System.out.println(monitorNode);
    }

    public static void serverReceived(byte order) {
        MonitorNode monitorNode = new MonitorNode(order, 2);
        monitorNodeList.add(monitorNode);
        System.out.println(monitorNode.toString());
    }

    public static void serverReturn(byte order) {
        MonitorNode monitorNode = new MonitorNode(order, 3);
        monitorNodeList.add(monitorNode);
        System.out.println(monitorNode);
    }

    public static void clientReceived(byte order) {
        MonitorNode monitorNode = new MonitorNode(order, 4);
        monitorNodeList.add(monitorNode);
        System.out.println(monitorNode);
    }

    public static void clientProcessed(byte order) {
        MonitorNode monitorNode = new MonitorNode(order, 5);
        monitorNodeList.add(monitorNode);
        System.out.println(monitorNode);
    }

    public static void displayProcess() {
        int maxOrder = monitorNodeList.stream().mapToInt(MonitorNode::getOrder).max().getAsInt();
        int maxSecond = monitorNodeList.stream().mapToInt(MonitorNode::getSecond).max().getAsInt();

        int[][] stepTable = new int[maxOrder][maxSecond];

        monitorNodeList.stream().forEach(monitorNode -> {
            stepTable[monitorNode.getOrder() - 1][monitorNode.getSecond() - 1] = monitorNode.getStep();
        });

        System.out.println("----- Monitor Process -----");
        System.out.print("连接\\时间\t");
        for (int second = 1; second <= maxSecond; second++) {
            System.out.print(second + "\t");
        }
        for (int order = 1; order <= maxOrder; order++) {
            System.out.print("\n连接" + order + "\t");
            for (int second = 1; second <= maxSecond; second++) {
                System.out.print((stepTable[order - 1][second - 1] == 0 ? "" : stepTable[order - 1][second - 1]) + "\t");
            }
        }
    }

    @Data
    static class MonitorNode {
        int second;
        int order;
        int step;
        long threadID;

        public MonitorNode(int order, int step) {
            this.second = (int) (Instant.now().getEpochSecond() - begin);
            this.order = order;
            this.step = step;
            this.threadID = Thread.currentThread().getId();
        }

        public String toString() {
            String process = null;
            switch (step) {
                case 1:
                    process = "CLIENT_SEND";
                    break;
                case 2:
                    process = "SERVER_RECEIVED";
                    break;
                case 3:
                    process = "SERVER_RETURN";
                    break;
                case 4:
                    process = "CLIENT_RECEIVED";
                    break;
                case 5:
                    process = "CLIENT_PROCESSED";
                    break;
            }

            return "[" + order + "][" + threadID + "][" + second + "][" + process + "]";
        }
    }
}
