package com.jerry.lab.concurrent.juc;

import com.jerry.lab.common.Utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {
    private double[] numbers;
    private int task_size;

    public long process(double[] numbers, int thread_size, int task_size, double total) {
        this.numbers = numbers;
        this.task_size = task_size;

        long begin = System.currentTimeMillis();
        ForkJoinPool forkJoinPool = new ForkJoinPool(thread_size);
        double result = forkJoinPool.invoke(new ForkJoinTask(0, numbers.length - 1));

        if (!Utils.isEqual(total, result)) {
            System.err.println("got error " + result + "|" + total);
        }
        long end = System.currentTimeMillis();

        forkJoinPool.shutdownNow();

        return end - begin;
    }

    private class ForkJoinTask extends RecursiveTask<Double> {
        private int first;
        private int last;
        private int count_per_task;

        public ForkJoinTask(int first, int last) {
            this.first = first;
            this.last = last;
            count_per_task = numbers.length / task_size;
        }

        protected Double compute() {
            double subCount = 0;
            if (last - first < count_per_task) {
                for (int i = first; i <= last; i++) {
                    Utils.calNumber(numbers[i]);
                    subCount += numbers[i];
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
