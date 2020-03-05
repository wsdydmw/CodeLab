package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {
    private double[] numbers;
    private int task_size;

    public long process(double[] numbers, int thread_size, int task_size) {
        this.numbers = numbers;
        this.task_size = task_size;

        long begin = System.currentTimeMillis();
        new ForkJoinPool(thread_size).invoke(new ForkJoinTask(0, numbers.length - 1));
        return System.currentTimeMillis() - begin;
    }

    private class ForkJoinTask extends RecursiveTask<Integer> {
        private int first;
        private int last;
        private int count_per_task;

        public ForkJoinTask(int first, int last) {
            this.first = first;
            this.last = last;
            count_per_task = numbers.length / task_size;
        }

        protected Integer compute() {
            int subCount;
            if (last - first < count_per_task) {
                subCount = 0;
                for (int i = first; i <= last; i++) {
                    if (numbers[i] < 0.5) subCount++;
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
