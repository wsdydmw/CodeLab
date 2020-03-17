package com.jerry.lab.concurrent.juc;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class ThreadPoolTest {
    private long[] numbers;

    public long process(long[] numbers, int thread_size, int task_size, long total) throws ExecutionException, InterruptedException {
        long result = 0;
        this.numbers = numbers;

        long begin = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(thread_size);

        int count_per_task = numbers.length / task_size;
        Future[] futureList = new Future[task_size];
        for (int i = 0; i <= task_size - 1; i++) {
            futureList[i] = executorService.submit(new ThreadPoolExecutorTask(i * count_per_task, (i + 1) * count_per_task - 1));
        }

        // wait all task complete
        for (int i = 0; i < task_size; i++) {
            result += Long.parseLong(futureList[i].get().toString());
        }

        if (total != result) {
            System.err.print("got error " + result + "|" + total);
        }
        long end = System.currentTimeMillis();

        executorService.shutdownNow();

        return end - begin;
    }


    private class ThreadPoolExecutorTask implements Callable<Long> {
        private int first;
        private int last;

        public ThreadPoolExecutorTask(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public Long call() {
            long sumCount = 0;
            for (int i = first; i <= last; i++) {
                Utils.calNumber(numbers[i]);
                sumCount += numbers[i];
            }
            return sumCount;
        }
    }
}

