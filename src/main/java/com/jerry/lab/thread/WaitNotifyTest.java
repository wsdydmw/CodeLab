package com.jerry.lab.thread;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaitNotifyTest {
    public static void main(String[] args) {
        Queue<Long> buffer = new LinkedList<Long>();
        int queueSize = 1;

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 1; i <= 4; i++) {
            if (i % 2 == 0) {
                executorService.execute(new Producer(buffer, queueSize, "PRODUCER" + i));
            } else {
                executorService.execute(new Consumer(buffer, queueSize, "CONSUMER" + i));
            }
        }
    }
}

/**
 * Producer Thread will keep producing values for Consumer Thread to consumer.
 * It will wait() when Queue is full and notify() when produced values
 */
class Producer extends Thread {
    private Queue<Long> queue;
    private int maxSize;

    public Producer(Queue<Long> queue, int maxSize, String name) {
        super(name);
        this.queue = queue;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                System.out.println(super.getName() + " get lock");
                while (queue.size() == maxSize) {//使用while而不是if，是因为被唤醒后需要再次检查条件
                    System.out.println("Queue is full, " + super.getName() + " need to wait and release lock");

                    try {
                        queue.wait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(super.getName() + " get lock and notified");//注意执行到这一步的，都是已经获取了queue对象锁
                }

                try {
                    sleep((int) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long now = Instant.now().toEpochMilli();
                queue.add(now);
                System.out.println(super.getName() + " producing value -> " + now);

                System.out.println(super.getName() + " notify others");
                queue.notifyAll();
            }
        }
    }
}

/**
 * Consumer Thread will keep consumering values from shared queue. It will
 * wait() when queue is empty and notify() when consumed value
 */
class Consumer extends Thread {
    private Queue<Long> queue;

    public Consumer(Queue<Long> queue, int maxSize, String name) {
        super(name);
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                System.out.println(super.getName() + " get lock");
                while (queue.isEmpty()) {
                    System.out.println("Queue is empty, " + super.getName() + " need to wait and release lock");

                    try {
                        queue.wait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(super.getName() + " get lock and notified");//注意执行到这一步的，都是已经获取了queue对象锁
                }

                try {
                    sleep((int) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(super.getName() + " consuming value : " + queue.remove());

                queue.notifyAll();
                System.out.println(super.getName() + " notify others");
            }
        }
    }
}
