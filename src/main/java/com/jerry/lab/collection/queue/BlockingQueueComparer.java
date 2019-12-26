package com.jerry.lab.collection.queue;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class BlockingQueueComparer {
    static int N = 5000000;//测试数据量

    public static void main(String[] args) throws Exception {
        System.out.println("length | Linked | Array | Synchronous-noFair | Synchronous-Fair | LinkedTransfer | Linked-C | Array-C | Synchronous-noFair-C | Synchronous-Fair-C | LinkedTransfer-C");
        System.out.println("-- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --");
        for (int i = 0; i < 10; i++) {
            int length = (int) Math.pow(3d, (double) i);// queue's length
            int concurrentDegree = 3;// number of input() and take() threads

            System.out.print(length + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<Integer>(length), 1)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<Integer>(length), 1)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(false), 1)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(true), 1)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedTransferQueue<Integer>(), 1)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<Integer>(length), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<Integer>(length), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(false), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(true), concurrentDegree)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedTransferQueue<Integer>(), concurrentDegree)));
            System.out.println();
        }
    }

    private static long doTest(final BlockingQueue<Integer> q, final int concurrentDegree) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentDegree * 2);
        CountDownLatch lastPutCount = new CountDownLatch(N);
        CountDownLatch lastTakeCount = new CountDownLatch(N);

        long begin = System.nanoTime();

        for (int i = 0; i < concurrentDegree; i++) {
            // put() threads
            executorService.submit(new Runnable() {
                public void run() {
                    int lastPut = 0;
                    while ((lastPut = (int) lastPutCount.getCount()) > 0) {
                        try {
                            q.put(lastPut);
                            lastPutCount.countDown();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            });

            // take() threads
            executorService.submit(new Runnable() {
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

        long cost = System.nanoTime() - begin;
        long result = (long) (1000000000.0 * N / cost);// items/second

        executorService.shutdown();

        return result;
    }
}
