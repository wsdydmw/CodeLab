package com.jerry.lab.collection.queue;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class BlockingQueueComparer {
    static int N = 5000000;//测试数据量

    public static void main(String[] args) throws Exception {
        System.out.println("length | LinkedBlockingQueue | ArrayBlockingQueue | SynchronousQueue | LinkedTransferQueue");
        System.out.println("-- | -- | -- | --");
        for (int i = 0; i < 10; i++) {
            int length = (int)Math.pow(3d, (double)i);// queue's length
            int concurrentDegree = 1;// number of input() and take() threads

            System.out.print(length + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<Integer>(length), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<Integer>(length), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedTransferQueue<>(), concurrentDegree)));
            System.out.println();
        }
    }

    private static long doTest(final BlockingQueue<Integer> q, final int concurrentDegree) throws Exception {
        ExecutorService putExecutorService = Executors.newFixedThreadPool(concurrentDegree);
        ExecutorService takeExecutorService = Executors.newFixedThreadPool(concurrentDegree);
        CountDownLatch lastPutCount = new CountDownLatch(N);
        CountDownLatch lastTakeCount = new CountDownLatch(N);

        long begin = System.currentTimeMillis();

        for(int i = 0; i < concurrentDegree; i++ ) {
            // put() threads
            putExecutorService.submit(new Runnable() {
                public void run() {
                    int lastPut = 0;
                    while ((lastPut = (int)lastPutCount.getCount()) > 0) {
                        try {
                            q.put(lastPut);
                            lastPutCount.countDown();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            });

            // take() threads
            takeExecutorService.submit(new Runnable() {
                public void run() {
                    while (lastTakeCount.getCount() > 0) {
                        try {
                            q.take();
                            lastTakeCount.countDown();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        lastPutCount.await();
        lastTakeCount.await();

        long cost = System.currentTimeMillis() - begin;
        long result = (long) (1000.0 * N / cost);// items/second

        putExecutorService.shutdown();
        takeExecutorService.shutdown();

        return result;
    }
}
