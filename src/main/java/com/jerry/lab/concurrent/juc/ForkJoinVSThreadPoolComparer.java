package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ExecutionException;
import java.util.stream.DoubleStream;

public class ForkJoinVSThreadPoolComparer {

    private static int NUMBER_SIZE = 60000000;
    private static int THREAD_SIZE = 4;
    private static double[] NUMBERS;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NUMBERS = DoubleStream.generate(Math::random).limit(NUMBER_SIZE).toArray();

        System.out.println("taskSize\tForkJoin\tThreadPool\tForkJoinPool");
        for (int i = 1; i <= 6; i++) {
            int task_size = (int) Math.pow(5d, i);// task size

            System.out.print(task_size + "\t");
            System.out.print(new ThreadPoolTest().process(NUMBERS, THREAD_SIZE, task_size) + "\t");
            System.gc();
            System.out.print(new ForkJoinTest().process(NUMBERS, THREAD_SIZE, task_size) + "\t");
            System.gc();
            System.out.println(new ForkJoinPoolTest().process(NUMBERS, THREAD_SIZE, task_size));
            System.gc();
        }
        System.exit(0);
    }
}
