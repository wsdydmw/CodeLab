package com.jerry.lab.thread;

public class SynchronizedVisibilityDemo {
    // 共享变量
    private boolean ready = false;

    public static void main(String[] args) {
        SynchronizedVisibilityDemo demo = new SynchronizedVisibilityDemo();
        demo.new ReadThread().start();
        demo.new WriteThread().start();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            System.out.println("[Read Thread]Begin to wait ready signal");
            while (true) {
                synchronized (this) {
                    if (ready) {
                        break;
                    }
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

            synchronized (this) {
                ready = true;
            }
            System.out.println("[Write Thread]Ready signal changed");
        }
    }
}