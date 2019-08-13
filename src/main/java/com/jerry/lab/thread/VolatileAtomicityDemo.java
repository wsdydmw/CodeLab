package com.jerry.lab.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class VolatileAtomicityDemo {
    static CountDownLatch finishCountDownLatch = new CountDownLatch(20 * 1000);

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        for (int i = 0; i < 20; i++) {
            new Thread() {
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        counter.increase();
                        finishCountDownLatch.countDown();
                    }
                }

            }.start();
        }

        finishCountDownLatch.await();

        System.out.println("result is " + (counter.get() == 20 * 1000 ? "correct" : "wrong"));
    }
}

class Counter {
    private int inc = 0;

    public synchronized void increase() {
        inc++;
    }

    public int get() {
        return inc;
    }
}

