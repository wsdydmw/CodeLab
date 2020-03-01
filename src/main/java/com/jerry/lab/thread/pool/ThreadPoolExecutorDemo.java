package com.jerry.lab.thread.pool;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        int taskNum = 3;
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(taskNum);
        CountDownLatch finishCountDown = new CountDownLatch(taskNum);

        for (int taskNo = 0; taskNo < taskNum; taskNo++) {
            executorService.scheduleAtFixedRate(new workThread(taskNo), 0, 3, TimeUnit.SECONDS);
            //finishCountDown.countDown();
        }

        //finishCountDown.await();
        //executorService.shutdown();
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
