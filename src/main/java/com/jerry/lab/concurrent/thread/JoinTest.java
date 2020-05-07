package com.jerry.lab.concurrent.thread;

import java.util.concurrent.TimeUnit;

public class JoinTest {
    public static void main(String[] args) throws Exception {
        Thread previous = Thread.currentThread();
        for (int i = 0; i < 10; i++) {
            // 每个线程拥有前一个线程的引用，需要等待前一个线程终止，才能从等待中返回
            Thread thread = new Thread(new Domino(previous), String.valueOf(i));
            thread.start();
            previous = thread;
            TimeUnit.SECONDS.sleep(2);
        }

        System.out.println("thread " + Thread.currentThread().getName() + " finish");
    }

    static class Domino implements Runnable {
        private Thread previous;

        public Domino(Thread previous) {
            this.previous = previous;
        }

        public void run() {
            try {
                System.out.println("thread " + Thread.currentThread().getName() + " need to wait " + previous.getName());
                previous.join();
                try {
                    Thread.sleep((int) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
            }
            System.out.println("thread " + Thread.currentThread().getName() + " finish");
        }
    }
}
