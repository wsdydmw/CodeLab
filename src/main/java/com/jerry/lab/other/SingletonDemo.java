package com.jerry.lab.other;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingletonDemo {
    public static void main(String args[]) throws InterruptedException {
        long begin = System.currentTimeMillis();
        int thread_num = 1000;
        CountDownLatch beginLatch = new CountDownLatch(1);
        CountDownLatch exitLatch = new CountDownLatch(thread_num);
        ExecutorService executorService = Executors.newFixedThreadPool(thread_num);

        HashSet<Integer> hashCodeSet = new HashSet<>();

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
                    hashCodeSet.add(singleton.hashCode());
                    exitLatch.countDown();
                }
            });
        }

        beginLatch.countDown();//let's begin

        exitLatch.await();

        System.out.println("use time is " + (System.currentTimeMillis() - begin));
        System.out.println("hashCodeSet size is " + hashCodeSet.size());
        System.exit(0);
    }
}

class Singleton {
    private static Singleton instance = null;

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }

        return instance;
    }
}
