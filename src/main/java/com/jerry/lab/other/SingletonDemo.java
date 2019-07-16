package com.jerry.lab.other;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingletonDemo {
    public static void main(String args[]) throws InterruptedException {
        long begin = System.currentTimeMillis();
        int thread_num = 100;
        CountDownLatch beginLatch = new CountDownLatch(1);
        CountDownLatch exitLatch = new CountDownLatch(thread_num);
        ExecutorService executorService = Executors.newFixedThreadPool(thread_num);

        for (int i = 0; i < thread_num; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        beginLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Singleton singleton = Singleton.getInstance();
                    System.out.println(singleton.hashCode());
                    exitLatch.countDown();
                }
            });
        }

        beginLatch.countDown();//let's begin

        exitLatch.await();

        System.out.println("use time is " + (System.currentTimeMillis() - begin));
        System.exit(0);
    }
}

class Singleton {
    private static class SingletonHolder {
        static Singleton INSTANCE;

        static {
            INSTANCE = new Singleton();
        }
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
