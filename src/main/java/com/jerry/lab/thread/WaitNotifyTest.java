package com.jerry.lab.thread;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class WaitNotifyTest {
    public static void main(String[] args) {
        Queue<Long> buffer = new LinkedList<Long>();
        int maxSize = 10;

        Thread producer1 = new Producer(buffer, maxSize, "PRODUCER1");
        Thread producer2 = new Producer(buffer, maxSize, "PRODUCER2");
        Thread producer3 = new Producer(buffer, maxSize, "PRODUCER3");
        Thread producer4 = new Producer(buffer, maxSize, "PRODUCER4");
        Thread consumer1 = new Consumer(buffer, maxSize, "CONSUMER1");
        Thread consumer2 = new Consumer(buffer, maxSize, "CONSUMER2");
        Thread consumer3 = new Consumer(buffer, maxSize, "CONSUMER3");
        Thread consumer4 = new Consumer(buffer, maxSize, "CONSUMER4");

        producer1.start();
        producer2.start();
        producer3.start();
        producer4.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();
        consumer4.start();
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
                while (queue.size() == maxSize) {//使用while而不是if，是因为被唤醒后需要再次检查条件
                    System.out.println("Queue is full, " + super.getName() + " need to wait");

                    try {
                        queue.wait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
                while (queue.isEmpty()) {
                    System.out.println("Queue is empty, " + super.getName() + " need to wait");

                    try {
                        queue.wait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
