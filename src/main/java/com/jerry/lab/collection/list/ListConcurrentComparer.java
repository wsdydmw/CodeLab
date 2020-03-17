package com.jerry.lab.collection.list;

import com.jerry.lab.common.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * write percent	arrayList	synchronizedList	copyOnWriteArrayList
 * 0%	2556	2325	2401
 * 2%	2387(not thread safe, expect 1000100000 but 97960	2364	7161
 * 4%	2381(not thread safe, expect 1000200000 but 196491	2418	178891
 * 6%	2377(not thread safe, expect 1000300000 but 296761	2300	82131
 */
public class ListConcurrentComparer {
    static int DATA_INIT_SIZE = 1000;
    static int OPERATE_NUM = 5000000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("write percent\tarrayList\tsynchronizedList\tcopyOnWriteArrayList");
        for (int i = 0; i <= 3; i++) {
            // 1. data init
            List<Long> arrayList = new ArrayList();
            List<Long> synchronizedList = Collections.synchronizedList(new ArrayList<>());
            List<Long> copyOnWriteArrayList = new CopyOnWriteArrayList();

            for (int index = 0; index < DATA_INIT_SIZE; index++) {
                arrayList.add((long) index);
                synchronizedList.add((long) index);
                copyOnWriteArrayList.add((long) index);
            }

            // 2. do test
            int writePercent = 0 + 2 * i;
            System.out.print(writePercent + "%\t");
            System.out.print(doTest(arrayList, writePercent) + "\t");
            System.out.print(doTest(synchronizedList, writePercent) + "\t");
            System.out.print(doTest(copyOnWriteArrayList, writePercent));
            System.out.println();
        }

        System.exit(0);
    }

    public static String doTest(List<Long> list, int writePercent) throws InterruptedException {
        System.gc();
        Thread.sleep(5000);

        StringBuffer result = new StringBuffer();
        // 1. simulate concurrent
        ExecutorService executorService = Executors.newCachedThreadPool();
        int readCount = OPERATE_NUM * (100 - writePercent) / 100;
        int writeCount = OPERATE_NUM * writePercent / 100;
        CountDownLatch readCountLatch = new CountDownLatch(readCount);
        CountDownLatch writeCountLatch = new CountDownLatch(writeCount);

        long begin = System.currentTimeMillis();
        // read thread
        new Thread(() -> {
            for (int i = 0; i < readCount; i++) {
                executorService.submit(() -> {
                    long value = Long.parseLong(list.get(getRandomIndex()).toString());
                    Utils.calNumber(value);
                    readCountLatch.countDown();
                });
            }
        }).start();

        // write thread
        new Thread(() -> {
            for (int i = 0; i < writeCount; i++) {
                executorService.submit(() -> {
                    list.add(0L);
                    writeCountLatch.countDown();
                });
            }
        }).start();

        readCountLatch.await();
        writeCountLatch.await();
        result.append(System.currentTimeMillis() - begin);

        // 2. check result
        if (DATA_INIT_SIZE + writeCount != list.size()) {
            result.append("(not thread safe, expect " + DATA_INIT_SIZE + writeCount + " but " + list.size());
        }
        return result.toString();
    }

    public static int getRandomIndex() {
        return (int) (Math.random() * DATA_INIT_SIZE);
    }

}
