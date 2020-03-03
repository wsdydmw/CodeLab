package com.jerry.lab.concurrent.juc;

import java.util.concurrent.ExecutionException;
import java.util.stream.DoubleStream;

public class ForkJoinVSThreadPoolComparer {

    private static int NUMBER_SIZE = 500000;
    private static int THREAD_SIZE = 4;
    private static double[] NUMBERS;

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        NUMBERS = DoubleStream.generate(Math::random).limit(NUMBER_SIZE).toArray();

        System.out.println("Fork : " + new ForkJoinTest().process(NUMBERS, THREAD_SIZE) + "ms");
        System.out.println("ThreadPool : " + new ThreadPoolTest().process(NUMBERS, THREAD_SIZE) + "ms");
        System.exit(0);
    }
}
