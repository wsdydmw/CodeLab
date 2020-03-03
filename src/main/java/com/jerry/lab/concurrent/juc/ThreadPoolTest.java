package com.jerry.lab.concurrent.juc;

import java.util.concurrent.*;

public class ThreadPoolTest {
    private double[] numbers;

    public long process(double[] numbers, int thread_size) throws ExecutionException, InterruptedException {
        this.numbers = numbers;

        long begin = System.currentTimeMillis();
        ExecutorService tpe = Executors.newFixedThreadPool(thread_size);
        Future[] futhreList = new Future[thread_size];
        int size = numbers.length / thread_size;
        for (int i = 0; i < thread_size - 1; i++) {
            futhreList[i] = tpe.submit(new ThreadPoolExecutorTask(i * size, (i + 1) * size - 1));
        }
        futhreList[thread_size - 1] = tpe.submit(new ThreadPoolExecutorTask((thread_size - 1) * size, numbers.length - 1));
        for (int i = 0; i < thread_size; i++) {
            Integer.parseInt(futhreList[i].get().toString());
        }

        return System.currentTimeMillis() - begin;
    }


    private class ThreadPoolExecutorTask implements Callable<Integer> {
        volatile int dummy;
        private int first;
        private int last;

        public ThreadPoolExecutorTask(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public Integer call() {
            int subCount = 0;
            for (int i = first; i <= last; i++) {
                if (numbers[i] < 0.5) {
                    subCount++;
                }
            }
            return subCount;
        }
    }
}

