package com.jerry.lab.collection.queue;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class BlockingQueueComparer {
    static int N = 5000;//测试数据量

    public static void main(String[] args) throws Exception {
        System.out.println("length\tLinked\tArray\tSynchronous-noFair\tSynchronous-Fair\tLinkedTransfer\tLinked-C\tArray-C\tSynchronous-noFair-C\tSynchronous-Fair-C\tLinkedTransfer-C");
        for (int i = 0; i <= 10; i++) {
            int length = (int) Math.pow(2d, (double) i);// queue's length
            int concurrentDegree = 10;// number of input() and take() threads

            System.out.print(length + "\t");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<>(length), 1)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<>(length), 1)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<>(false), 1)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<>(true), 1)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new LinkedTransferQueue<>(), 1)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<>(length), concurrentDegree)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<>(length), concurrentDegree)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<>(false), concurrentDegree)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<>(true), concurrentDegree)) + "\t");
            System.out.print(Utils.displayNumber(doTest(new LinkedTransferQueue<>(), concurrentDegree)));
            System.out.println();
        }

        System.exit(0);
    }

    private static long doTest(final BlockingQueue<Integer> q, final int concurrentDegree) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentDegree * 2);
        CountDownLatch lastPutCount = new CountDownLatch(N);
        CountDownLatch lastTakeCount = new CountDownLatch(N);

        long begin = System.nanoTime();

        for (int i = 0; i < concurrentDegree; i++) {
            // put() threads
            executorService.execute(() -> {
                int lastPut = 0;
                while ((lastPut = (int) lastPutCount.getCount()) > 0) {
                    try {
                        q.put(lastPut);
                        lastPutCount.countDown();
                    } catch (InterruptedException ex) {
                    }
                }
            });

            // take() threads
            executorService.execute(() -> {
                while (lastTakeCount.getCount() > 0) {
                    try {
                        q.take();
                        lastTakeCount.countDown();
                    } catch (InterruptedException e) {
                    }
                }
            });
        }

        lastPutCount.await();
        lastTakeCount.await();

        long cost = System.nanoTime() - begin;
        long result = (long) (1000000000.0 * N / cost);// items/second

        executorService.shutdownNow();

        return result;
    }
}
