package com.jerry.lab.collection.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * 1. 为什么初次执行的函数会比较耗时
 * 2. synchronizedList居然和ArrayList差不多
 */
public class ListConcurrentComparer {
    static int DATA_SIZE = 100000;
    static int OPERATE_NUM = 20000;
    static int CONCURRENT_DEGREE = 1;

    public static void main(String[] args) throws InterruptedException {
        //System.out.println("writePercent\tCopyOnWriteArrayList\tsynchronizedArrayList\tArrayList");

        Thread.sleep(5000);
        for (int i = 0; i <= 3; i++) {
            int writePercent = 1 + 2 * i;
            System.out.print(writePercent + "%\n");
            System.out.print(doTest(Collections.synchronizedList(new ArrayList<>()), writePercent) + "\t");
            System.out.print(doTest(new CopyOnWriteArrayList(), writePercent) + "\t");
        System.out.print(doTest(new ArrayList<>(), writePercent) + "\t");
            System.out.println();
        }
    }

    public static long doTest(List<Integer> list, int writePercent) throws InterruptedException {
        // 1. data init
        for (int i = 0; i < DATA_SIZE; i++) {
            list.add(getRandomValue());
        }

        // 2. simulate concurrent
        //ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_DEGREE * 2);
        CountDownLatch writeCount = new CountDownLatch(OPERATE_NUM * writePercent / 100);
        CountDownLatch readCount = new CountDownLatch(OPERATE_NUM * (100 - writePercent) / 100);
        //System.out.println("read/write => " + readCount.getCount() + "/" + writeCount.getCount());

        long begin = System.currentTimeMillis();
        for (int threadIndex = 0; threadIndex < CONCURRENT_DEGREE; threadIndex++) {
            // read thread
            new Thread(() -> {
                //System.out.println("read thread" + Thread.currentThread().getId());
                while (readCount.getCount() > 0) {
                    int value = Integer.parseInt(list.get(getRandomIndex()).toString());
                    for (int i = 0 ; i< Integer.MAX_VALUE ; i ++) {//耗时操作
                        if(value <= i) {
                            readCount.countDown();
                            //System.out.println("read" + readCount.getCount());
                            break;
                        }
                    }
                }
            }).start();

            // write thread
            new Thread(() -> {
                //System.out.println("write thread" + Thread.currentThread().getId());
                while (writeCount.getCount() > 0) {
                    int index = getRandomIndex();
                    list.set(index, getRandomValue());
                    writeCount.countDown();
                    //System.out.println("write"+ writeCount.getCount());
                }
            }).start();
        }

        readCount.await();
        writeCount.await();
        //System.out.println("over");

        long result = System.currentTimeMillis() - begin;
        //executorService.shutdownNow();

        return result;
    }

    public static int getRandomIndex() {
        return (int) (Math.random() * DATA_SIZE);
    }

    public static int getRandomValue() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }
}
