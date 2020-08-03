package com.jerry.lab.concurrent.thread;

public class ThreadLocalTest1 {
    static int THREAD_NUM = 5;

    //创建一个Integer型的线程本地变量
    public static final ThreadLocal<Integer> localCount = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static void main(String[] args) {
        Thread[] threads = new Thread[THREAD_NUM];
        for (int j = 0; j < THREAD_NUM; j++) {
            final int addCount = j;
            threads[j] = new Thread(() -> {
                //获取当前线程的本地变量
                int num = localCount.get();
                System.out.println(Thread.currentThread().getName() + " init count is " + localCount.get());
                //累加【线程编号】次，重新存入本地变量
                localCount.set(num + addCount);
                System.out.println(Thread.currentThread().getName() + " updated count is " + localCount.get());
            }, "Thread-" + j);
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }
}