package com.jerry.lab.threadpool;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        int taskNum = 5;
        ExecutorService executorService = Executors.newScheduledThreadPool(3);
        CountDownLatch finishCountDown = new CountDownLatch(taskNum);

        for (int taskNo = 0; taskNo < taskNum; taskNo++) {
            executorService.execute(new workThread(taskNo));
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
        System.out.println("task[" + order + "] begin running at thread " + Thread.currentThread().getId() + ", time is " + LocalDateTime.now().toLocalTime());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("task[" + order + "] end running at thread " + Thread.currentThread().getId() + ", time is " + LocalDateTime.now().toLocalTime());
    }
}
