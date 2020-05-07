package com.jerry.lab.concurrent.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class UserThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger nextId = new AtomicInteger();

    //定义线程组名称
    UserThreadFactory(String whatFeatureOfGroup) {
        namePrefix = whatFeatureOfGroup + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable task) {
        String name = namePrefix + nextId.getAndIncrement();
        Thread thread = new Thread(null, task, name, 0);
        System.out.println("newThread " + thread.getName());
        return thread;
    }
}

class Task implements Runnable {
    private int order;

    Task(int order) {
        this.order = order;
    }

    @Override
    public void run() {
        System.out.println(this.toString() + " running on " + Thread.currentThread().getName());
        try {
            Thread.sleep(Math.round(Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "task" + order;
    }
}