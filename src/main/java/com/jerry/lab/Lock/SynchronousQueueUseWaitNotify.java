package com.jerry.lab.Lock;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class SynchronousQueueUseWaitNotify {
    public static void main(String[] args) {
        Queue<Long> queue = new LinkedList<Long>();
        int maxSize = 1;

        Thread producer1 = new ProducerW(queue, maxSize, "PRODUCER1");
        Thread consumer1 = new ConsumerW(queue, "CONSUMER1");

        producer1.start();
        consumer1.start();
    }
}

class ProducerW extends Thread {
    private Queue<Long> queue;
    private int maxSize;

    public ProducerW(Queue<Long> queue, int maxSize, String name) {
        super(name);
        this.queue = queue;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                System.out.println(super.getName() + " get lock");
                while (!queue.isEmpty()) {//使用while而不是if，是因为被唤醒后需要再次检查条件
                    System.out.println("Queue have value " + queue.peek() + ", " + super.getName() + " need to wait");
                    try {
                        queue.wait();
                        System.out.println(super.getName() + " has been notified");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                try {
                    sleep((int) (Math.random() * 1000));//do some operate
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long now = Instant.now().toEpochMilli();
                queue.add(now);
                System.out.println(super.getName() + " producing value -> " + now);

                queue.notifyAll();
                System.out.println(super.getName() + " notify others");
            }
        }
    }
}

class ConsumerW extends Thread {
    private Queue<Long> queue;

    public ConsumerW(Queue<Long> queue, String name) {
        super(name);
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                System.out.println(super.getName() + " get lock");
                while (queue.isEmpty()) {
                    System.out.println("Queue is empty, " + super.getName() + " need to wait");

                    try {
                        queue.wait();
                        System.out.println(super.getName() + "has been notified");
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
