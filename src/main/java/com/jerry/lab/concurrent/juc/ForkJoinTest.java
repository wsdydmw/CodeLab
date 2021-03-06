package com.jerry.lab.concurrent.juc;

import com.jerry.lab.common.Utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {
    private long[] numbers;
    private int task_size;

    public String process(long[] numbers, int thread_size, int task_size, long total) {
        StringBuffer result = new StringBuffer();
        this.numbers = numbers;
        this.task_size = task_size;

        long begin = System.currentTimeMillis();
        ForkJoinPool forkJoinPool = new ForkJoinPool(thread_size);
        long _total = forkJoinPool.invoke(new ForkJoinTask(0, numbers.length - 1));
        long end = System.currentTimeMillis();

        result.append((end - begin));
        if (total != _total) {
            result.append("(got error " + _total + "|" + total);
        }

        forkJoinPool.shutdownNow();

        return result.toString();
    }

    private class ForkJoinTask extends RecursiveTask<Long> {
        private int first;
        private int last;
        private int count_per_task;

        public ForkJoinTask(int first, int last) {
            this.first = first;
            this.last = last;
            count_per_task = numbers.length / task_size;
        }

        protected Long compute() {
            long sumCount = 0;
            if (last - first < count_per_task) {
                for (int i = first; i <= last; i++) {
                    Utils.calNumber(numbers[i]);
                    sumCount += numbers[i];
                }
            } else {
                int mid = (first + last) >>> 1;

                ForkJoinTask left = new ForkJoinTask(first, mid);
                left.fork();

                ForkJoinTask right = new ForkJoinTask(mid + 1, last);
                right.fork();

                sumCount = left.join();
                sumCount += right.join();
            }
            return sumCount;
        }
    }
}
