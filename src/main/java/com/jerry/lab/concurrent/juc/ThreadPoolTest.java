package com.jerry.lab.concurrent.juc;

import java.util.concurrent.*;

public class ThreadPoolTest {
    private double[] numbers;

    public long process(double[] numbers, int thread_size, int task_size) throws ExecutionException, InterruptedException {
        this.numbers = numbers;

        long begin = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(thread_size);

        int count_per_task = numbers.length / task_size;
        Future[] futureList = new Future[task_size];
        for (int i = 0; i <= task_size - 1; i++) {
            futureList[i] = executorService.submit(new ThreadPoolExecutorTask(i * count_per_task, (i + 1) * count_per_task - 1));
        }

        // wait all task complete
        for (int i = 0; i < thread_size; i++) {
            Integer.parseInt(futureList[i].get().toString());
        }

        return System.currentTimeMillis() - begin;
    }


    private class ThreadPoolExecutorTask implements Callable<Integer> {
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

