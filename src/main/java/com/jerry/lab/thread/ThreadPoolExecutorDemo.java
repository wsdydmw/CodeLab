package com.jerry.lab.thread;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        int worksNum = 10;
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch finishCountDown = new CountDownLatch(worksNum);

        for (int i = 0; i < worksNum; i++) {
            executorService.execute(new workThread(i));
            finishCountDown.countDown();
        }

        finishCountDown.await();

        executorService.shutdown();

    }
}

class workThread implements Runnable {
    int order;

    public workThread(int order) {
        this.order = order;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[" + LocalDateTime.now().toLocalTime() + "][" + Thread.currentThread().getId() + "]" + this.order);
    }
}
