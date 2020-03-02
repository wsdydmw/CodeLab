package com.jerry.lab.concurrent.synchronization;

public class VolatileVisibilityDemo {
    // 共享变量
    private volatile boolean ready = false;

    public static void main(String[] args) {
        VolatileVisibilityDemo demo = new VolatileVisibilityDemo();
        demo.new ReadThread().start();
        demo.new WriteThread().start();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            System.out.println("[Read Thread]Begin to wait ready signal");
            while (true) {
                if (ready) {
                    break;
                }
            }
            System.out.println("[Read Thread]Can read now");
        }
    }

    private class WriteThread extends Thread {

        @Override
        public void run() {
            // 使读进程有机会先执行
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }

            ready = true;
            System.out.println("[Write Thread]Ready signal changed");
        }
    }
}