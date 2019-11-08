package com.jerry.lab.lock;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueUseCondition {
    public static void main(String args[]) {
        Queue<Long> queue = new LinkedList<Long>();

        final Lock lock = new ReentrantLock();// 锁对象
        final Condition producerCondition = lock.newCondition();// 写线程条件
        final Condition consumerCondition = lock.newCondition();// 读线程条件

        Thread producer1 = new ProducerUseCondition(queue, lock, producerCondition, consumerCondition, "PRODUCER1");
        Thread consumer1 = new ConsumerUseCondition(queue, lock, producerCondition, consumerCondition, "CONSUMER1");

        producer1.start();
        consumer1.start();
    }
}

/**
 * Producer Thread will keep producing values for Consumer Thread to consumer.
 * It will wait() when Queue is full and notify() when produced values
 */
class ProducerUseCondition extends Thread {
    private Queue<Long> queue;
    private Lock lock;
    private Condition producerCondition;
    private Condition consumerCondition;

    public ProducerUseCondition(Queue<Long> queue, Lock lock, Condition producerCondition, Condition consumerCondition, String name) {
        super(name);
        this.queue = queue;
        this.lock = lock;
        this.producerCondition = producerCondition;
        this.consumerCondition = consumerCondition;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();//Before waiting on the condition the lock must be held by the current thread

            try {
                System.out.println(super.getName() + " get lock");

                while (!queue.isEmpty()) {// 使用while而不是if，是因为被唤醒后需要再次检查条件
                    System.out.println("Queue has value, " + queue.peek() + ", " + super.getName() + " need to wait");
                    producerCondition.await();
                }

                currentThread().sleep((int) (Math.random() * 1000));

                long now = Instant.now().toEpochMilli();
                System.out.println(super.getName() + " producing value : " + now);
                queue.add(now);

                System.out.println(super.getName() + " will notify consumer thread only");
                consumerCondition.signal();

                System.out.println(super.getName() + " will make producer thread wait only");
                producerCondition.await();
                System.out.println(super.getName() + " has been notified");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
}

/**
 * Consumer Thread will keep consumering values from shared queue. It will
 * wait() when queue is empty and notify() when consumed value
 */
class ConsumerUseCondition extends Thread {
    private Queue<Long> queue;
    private Lock lock;
    private Condition producerCondition;
    private Condition consumerCondition;

    public ConsumerUseCondition(Queue<Long> queue, Lock lock, Condition producerCondition, Condition consumerCondition, String name) {
        super(name);
        this.queue = queue;
        this.lock = lock;
        this.producerCondition = producerCondition;
        this.consumerCondition = consumerCondition;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();

            try {
                System.out.println(super.getName() + " get lock");

                while (queue.isEmpty()) {
                    System.out.println("Queue is empty, " + super.getName() + " need to wait");
                    consumerCondition.await();
                }

                currentThread().sleep((int) (Math.random() * 1000));

                System.out.println(super.getName() + " consuming value : " + queue.remove());

                System.out.println(super.getName() + " will notify producer thread only");
                producerCondition.signal();

                System.out.println(super.getName() + " will make consumer thread wait only");
                consumerCondition.await();
                System.out.println(super.getName() + " has been notified");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
}