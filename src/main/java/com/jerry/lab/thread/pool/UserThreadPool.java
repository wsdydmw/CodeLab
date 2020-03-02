package com.jerry.lab.thread.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UserThreadPool {
    public static void main(String[] args) {
        BlockingQueue queue = new LinkedBlockingQueue(2);
        UserThreadFactory userThreadFactory = new UserThreadFactory("UserThreadFactory-1");
        UserRejectHandler handler = new UserRejectHandler();

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1, 2, 60,
                        TimeUnit.SECONDS, queue, userThreadFactory, handler);

        for (int i = 0; i < 10; i++) {
            Runnable task = new Task(i);
            threadPoolExecutor.execute(task);
        }

        threadPoolExecutor.shutdown();
    }
}
