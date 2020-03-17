package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ForkJoinVSThreadPoolComparer {

    private static int NUMBER_SIZE = 2000000;
    private static int THREAD_SIZE = 32;
    private static long[] NUMBERS = new long[NUMBER_SIZE];

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        IntStream.range(0, NUMBER_SIZE).forEach((index) -> {
            NUMBERS[index] = index;
        });
        long total = LongStream.of(NUMBERS).sum();// use to check result's validity

        System.out.println("taskSize\tThreadPool\tForkJoin\tForkJoinPool");
        for (int i = 1; i <= 10; i++) {
            int task_size = (int) Math.pow(2d, i);// task size
            System.out.print(task_size + "\t");

            System.gc();
            Thread.sleep(3000);
            System.out.print(new ThreadPoolTest().process(NUMBERS, THREAD_SIZE, task_size, total) + "\t");

            System.gc();
            Thread.sleep(3000);
            System.out.print(new ForkJoinTest().process(NUMBERS, THREAD_SIZE, task_size, total) + "\t");

            System.gc();
            Thread.sleep(3000);
            System.out.print(new ForkJoinPoolTest().process(NUMBERS, THREAD_SIZE, task_size, total));
            System.out.println();
        }
        System.exit(0);
    }
}
