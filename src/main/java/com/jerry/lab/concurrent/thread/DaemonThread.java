package com.jerry.lab.concurrent.thread;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

public class DaemonThread {
    static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) {
        DaemonThread daemonThread = new DaemonThread();
        InputThread inputThread = daemonThread.new InputThread();
        inputThread.start();

        PrintThread printThread = daemonThread.new PrintThread();
        printThread.setDaemon(true);
        printThread.start();

        System.out.println("退出");
    }

    class InputThread extends Thread {

        @Override
        public void run() {
            System.out.println("请开始输入");
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                String str = sc.next();

                if (str.equals("exit")) {
                    System.out.println("输入线程退出");
                    return;
                } else {
                    queue.add(str);
                }
            }
        }
    }

    class PrintThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("打印：" + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
