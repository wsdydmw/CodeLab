package com.jerry.lab.concurrent.juc;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class ForkJoinPoolTest {
    private long[] numbers;

    public String process(long[] numbers, int thread_size, int task_size, long total) throws ExecutionException, InterruptedException {
        StringBuffer result = new StringBuffer();
        long _total = 0;
        this.numbers = numbers;

        long begin = System.currentTimeMillis();
        ExecutorService executorService = Executors.newWorkStealingPool(thread_size);

        int count_per_task = numbers.length / task_size;
        Future[] futureList = new Future[task_size];
        for (int i = 0; i <= task_size - 1; i++) {
            if (i == task_size - 1) {
                futureList[i] = executorService.submit(new ForkJoinPoolTest.ThreadPoolExecutorTask(i * count_per_task, numbers.length - 1));
            } else {
                futureList[i] = executorService.submit(new ForkJoinPoolTest.ThreadPoolExecutorTask(i * count_per_task, (i + 1) * count_per_task - 1));
            }
        }

        // wait all task complete
        for (int i = 0; i < task_size; i++) {
            _total += Long.parseLong(futureList[i].get().toString());
        }
        long end = System.currentTimeMillis();

        result.append((end - begin));
        if (total != _total) {
            result.append("(got error " + _total + "|" + total);
        }

        executorService.shutdownNow();

        return result.toString();
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

