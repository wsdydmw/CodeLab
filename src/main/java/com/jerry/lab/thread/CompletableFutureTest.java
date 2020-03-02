package com.jerry.lab.thread;

import java.util.concurrent.*;

/**
 * 基于jdk1.8实现任务异步处理
 */
public class CompletableFutureTest {
    public static void main(String[] args) throws Throwable, ExecutionException {
        // 两个线程的线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // CompletableFuture
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("[" + Thread.currentThread().getId() + "]Task started!");
            try {
                //模拟耗时操作
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[" + Thread.currentThread().getId() + "]Task end!");
            return;
        }, executor);

        //采用lambada的实现方式
        future.thenRun(() -> System.out.println("[" + Thread.currentThread().getId() + "]Task got result"));

        System.out.println("[" + Thread.currentThread().getId() + "]Main thread is running ");
    }
}
