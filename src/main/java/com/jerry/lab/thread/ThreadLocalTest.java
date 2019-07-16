package com.jerry.lab.thread;

public class ThreadLocalTest {
    //创建一个Integer型的线程本地变量
    public static final ThreadLocal<Integer> localCount = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static void main(String[] args) throws InterruptedException {
        int thread_num = 5;
        Thread[] threads = new Thread[thread_num];
        for (int j = 0; j < thread_num; j++) {
            threads[j] = new Thread(new Runnable() {
                @Override
                public void run() {
                    //获取当前线程的本地变量，然后累加5次
                    int num = localCount.get();
                    for (int i = 0; i < 3; i++) {
                        num++;
                    }
                    //重新设置累加后的本地变量
                    localCount.set(num);
                    System.out.println(Thread.currentThread().getName() + " count is " + localCount.get());

                }
            }, "Thread-" + j);
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }
}