package com.jerry.lab.collection.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 比较
 */
public class ListConcurrentComparer {
    static int DATA_SIZE = 200000;
    static int OPERATE_NUM = 10000;
    static int CONCURRENT_DEGREE = 3;

    public static void main(String args[]) throws InterruptedException {
        System.out.println("writePercent\tCopyOnWriteArrayList\tsynchronizedArrayList");

        for (int i = 0; i <= 5; i++) {
            int writePercent = 0 + 2 * i;
            System.out.print(writePercent + "%\t");
            System.out.print(doTest(new CopyOnWriteArrayList(), writePercent) + "\t");
            System.out.print(doTest(Collections.synchronizedList(new ArrayList<>()),writePercent) + "\t");
            System.out.println();
        }
    }

    public static long doTest(List list, int writePercent) throws InterruptedException {
        // 1. data init
        for (int i = 0; i < DATA_SIZE; i++) {
            list.add(getRandomValue());
        }

        // 2. simulate concurrent
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_DEGREE * 2);
        CountDownLatch writeCount = new CountDownLatch((int)(OPERATE_NUM * writePercent / 100));
        CountDownLatch readCount = new CountDownLatch((int)(OPERATE_NUM * (1- writePercent / 100)));

        long begin = System.currentTimeMillis();
        for (int i = 0; i < CONCURRENT_DEGREE; i++) {
            // read thread
            executorService.execute(() -> {
                while (readCount.getCount() > 0) {
                    readCount.countDown();
                    list.get(getRandomIndex());
                }
            });

            // write thread
            executorService.execute(() -> {
                while (writeCount.getCount() > 0) {
                    writeCount.countDown();
                    list.set(getRandomIndex(), getRandomValue());
                }
            });
        }

        readCount.await();
        writeCount.await();

        long result = System.currentTimeMillis() - begin;
        executorService.shutdownNow();

        return result;
    }

    public static int getRandomIndex() {
        return (int)(Math.random() * DATA_SIZE);
    }

    public static int getRandomValue() {
        return (int)(Math.random() * Integer.MAX_VALUE);
    }
}
