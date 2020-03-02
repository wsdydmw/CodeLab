package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {
    private double[] numbers;

    public long process(double[] numbers, int thread_size) {
        this.numbers = numbers;

        long begin = System.currentTimeMillis();
        new ForkJoinPool(thread_size).invoke(new ForkJoinTask(0, numbers.length - 1));
        return System.currentTimeMillis() - begin;
    }

    private class ForkJoinTask extends RecursiveTask<Integer> {
        volatile int dummy;
        private int first;
        private int last;

        public ForkJoinTask(int first, int last) {
            this.first = first;
            this.last = last;
        }

        protected Integer compute() {
            int subCount;
            if (last - first < 10) {
                subCount = 0;
                for (int i = first; i <= last; i++) {
                    if (numbers[i] < 0.5) subCount++;

                    // 模拟任务量不均衡
                    try {
                        Thread.sleep((long) i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < numbers.length - i; j++) {
                        for (int k = 0; k < 100; k++) {
                            dummy = j * k + i; // dummy is volatile, so multiple writes occur
                            numbers[i] = dummy;
                        }
                    }
                }
            } else {
                int mid = (first + last) >>> 1;

                ForkJoinTask left = new ForkJoinTask(first, mid);
                left.fork();

                ForkJoinTask right = new ForkJoinTask(mid + 1, last);
                right.fork();

                subCount = left.join();
                subCount += right.join();
            }
            return subCount;
        }
    }
}
