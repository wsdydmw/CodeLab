package com.jerry.lab.concurrent.juc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于jdk1.8实现任务异步处理
 */
public class CompletableFutureTest {
    public static void main(String[] args) throws Throwable, ExecutionException {
        // 两个线程的线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // CompletableFuture
        CompletableFuture.runAsync(() -> {
            System.out.println("[" + Thread.currentThread().getId() + "]Task started!");
            try {
                //模拟耗时操作
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[" + Thread.currentThread().getId() + "]Task end!");
            return;
        }, executor)
                .thenRun(() -> System.out.println("[" + Thread.currentThread().getId() + "]Task got result"))
                .thenRun(() -> System.out.println("[" + Thread.currentThread().getId() + "]Task got result2"));

        System.out.println("[" + Thread.currentThread().getId() + "]Main thread is running ");
    }
}
