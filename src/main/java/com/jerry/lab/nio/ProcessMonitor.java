package com.jerry.lab.nio;

import java.time.LocalTime;

public class ProcessMonitor {

    public static void serverReceived (String param) {
        System.out.println("[Server][" + Thread.currentThread().getId() + "][" + LocalTime.now() +"] Received : " + param);
    }

    public static void serverReturn (String param) {
        System.out.println("[Server][" + Thread.currentThread().getId() + "][" + LocalTime.now() +"] Return : " + param);
    }

    public static void clientSend (String param) {
        System.out.println("[Client][" + Thread.currentThread().getId() + "][" + LocalTime.now() +"] Send : " + param);
    }

    public static void clientReceived (String param) {
        System.out.println("[Client][" + Thread.currentThread().getId() + "][" + LocalTime.now() +"] Received : " + param);
    }
}
