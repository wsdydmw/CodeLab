package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ExecutionException;
import java.util.stream.DoubleStream;

public class ForkJoinVSThreadPoolComparer {

    private static int NUMBER_SIZE = 50000000;
    private static int THREAD_SIZE = 32;
    private static double[] NUMBERS;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NUMBERS = DoubleStream.generate(Math::random).limit(NUMBER_SIZE).toArray();
        double total = DoubleStream.of(NUMBERS).sum();// use to check result's validity

        System.out.println("taskSize\tForkJoin\tThreadPool\tForkJoinPool");
        for (int i = 1; i <= 6; i++) {
            int task_size = (int) Math.pow(5d, i);// task size
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
