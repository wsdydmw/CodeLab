package com.jerry.lab.concurrent.juc;

import com.jerry.lab.common.Utils;

import java.util.concurrent.*;

public class ThreadPoolTest {
    private double[] numbers;

    public long process(double[] numbers, int thread_size, int task_size, double total) throws ExecutionException, InterruptedException {
        double result = 0;
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
            result += Double.parseDouble(futureList[i].get().toString());
        }

        if (!Utils.isEqual(total, result)) {
            System.err.println("got error " + result + "|" + total);
        }
        long end = System.currentTimeMillis();

        executorService.shutdownNow();

        return end - begin;
    }


    private class ThreadPoolExecutorTask implements Callable<Double> {
        private int first;
        private int last;

        public ThreadPoolExecutorTask(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public Double call() {
            double subCount = 0;
            for (int i = first; i <= last; i++) {
                Utils.calNumber(numbers[i]);
                subCount += numbers[i];
            }
            return subCount;
        }
    }
}

