package com.jerry.lab.collection.queue;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class BlockingQueueComparer {
    static ExecutorService e = Executors.newFixedThreadPool(2);
    static int N = 1000000;

    public static void main(String[] args) throws Exception {
        System.out.println("length | LinkedBlockingQueue | ArrayBlockingQueue | SynchronousQueue");
        System.out.println("-- | -- | -- | --");
        for (int i = 0; i < 3; i++) {
            int length = (i == 0) ? 1 : i * 10;
            System.out.print(length + " | ");
            System.out.print(Utils.displayNumber(doTest(new LinkedBlockingQueue<Integer>(length), N)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new ArrayBlockingQueue<Integer>(length), N)) + " | ");
            System.out.print(Utils.displayNumber(doTest(new SynchronousQueue<Integer>(), N)));
            System.out.println();
        }

        e.shutdown();
    }

    private static long doTest(final BlockingQueue<Integer> q, final int n) throws Exception {
        long t = System.currentTimeMillis();

        e.submit(new Runnable() {
            public void run() {
                for (int i = 0; i < n; i++)
                    try {
                        q.put(i);
                    } catch (InterruptedException ex) {
                    }
            }
        });

        Long r = e.submit(new Callable<Long>() {
            public Long call() {
                long sum = 0;
                for (int i = 0; i < n; i++)
                    try {
                        sum += q.take();
                    } catch (InterruptedException ex) {
                    }
                return sum;
            }
        }).get();
        t = System.currentTimeMillis() - t;

        return (long) (1000.0 * N / t); // items/second
    }
}
