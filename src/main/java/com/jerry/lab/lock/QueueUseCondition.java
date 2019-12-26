package com.jerry.lab.lock;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者与消费者交替执行
 */
public class QueueUseCondition {
    public static void main(String[] args) {
        Queue<Long> queue = new LinkedList<Long>();

        final Lock lock = new ReentrantLock();// 锁对象
        final Condition emptyCondition = lock.newCondition();// empty，可写不可读
        final Condition notEmptyCondition = lock.newCondition();// notEmpty，可读不可写

        Thread producer1 = new ProducerUseCondition(queue, lock, emptyCondition, notEmptyCondition, "PRODUCER1");
        Thread consumer1 = new ConsumerUseCondition(queue, lock, emptyCondition, notEmptyCondition, "CONSUMER1");

        producer1.start();
        consumer1.start();
    }
}

class ProducerUseCondition extends Thread {
    private Queue<Long> queue;
    private Lock lock;
    private Condition emptyCondition;
    private Condition notEmptyCondition;

    public ProducerUseCondition(Queue<Long> queue, Lock lock, Condition emptyCondition, Condition notEmptyCondition, String name) {
        super(name);
        this.queue = queue;
        this.lock = lock;
        this.emptyCondition = emptyCondition;
        this.notEmptyCondition = notEmptyCondition;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();//必须先获取锁

            try {
                System.out.println(super.getName() + " get lock");

                while (queue.isEmpty()) {// 使用while而不是if，是因为被唤醒后需要再次检查条件
                    sleep((int) (Math.random() * 1000));

                    long now = Instant.now().toEpochMilli();
                    System.out.println(super.getName() + " producing value : " + now);
                    queue.add(now);

                    System.out.println("Queue already has data, " + super.getName() + " need to wake up consumer and make producer wait");
                    notEmptyCondition.signal();
                    emptyCondition.await();//相关联的锁自动释放
                    System.out.println(super.getName() + " has been notified");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(super.getName() + " release lock");
                lock.unlock();
            }
        }
    }
}

class ConsumerUseCondition extends Thread {
    private Queue<Long> queue;
    private Lock lock;
    private Condition emptyCondition;
    private Condition notEmptyCondition;

    public ConsumerUseCondition(Queue<Long> queue, Lock lock, Condition emptyCondition, Condition notEmptyCondition, String name) {
        super(name);
        this.queue = queue;
        this.lock = lock;
        this.emptyCondition = emptyCondition;
        this.notEmptyCondition = notEmptyCondition;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();

            try {
                System.out.println(super.getName() + " get lock");

                while (!queue.isEmpty()) {
                    sleep((int) (Math.random() * 1000));
                    System.out.println(super.getName() + " consuming value : " + queue.remove());

                    System.out.println("Queue is empty, " + super.getName() + " need to wake up producer and make consumer wait");
                    emptyCondition.signal();
                    notEmptyCondition.await();
                    System.out.println(super.getName() + " has been notified");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(super.getName() + " release lock");
                lock.unlock();
            }
        }
    }
}