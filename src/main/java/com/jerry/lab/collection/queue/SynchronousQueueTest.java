package com.jerry.lab.collection.queue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class SynchronousQueueTest {
    public static void main(String[] args) {
        SynchronousQueue<Integer> queue = new SynchronousQueue<Integer>();// 公平交易
        Producer p1 = new Producer("p1", queue, 10);
        Producer p2 = new Producer("p2", queue, 20);
        Producer p3 = new Producer("p2", queue, 30);

        Consumer c1 = new Consumer("c1", queue);
        Consumer c2 = new Consumer("c2", queue);
        Consumer c3 = new Consumer("c3", queue);

        Object[] objects = new Object[]{p1, p2, c1, p3, c2, c3};

        for (Object o : objects) {
            if (o instanceof Producer) {
                ((Producer) o).start();
            } else {
                ((Consumer) o).start();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Producer extends Thread {
        private SynchronousQueue<Integer> queue;
        private int n;

        public Producer(String name, SynchronousQueue<Integer> queue, int n) {
            super(name);
            this.queue = queue;
            this.n = n;
        }

        public void run() {
            System.out.println(getName() + " begin put");
            try {
                queue.put(n);
                System.out.println(getName() + " have putted " + n);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer extends Thread {
        private SynchronousQueue<Integer> queue;

        public Consumer(String name, SynchronousQueue<Integer> queue) {
            super(name);
            this.queue = queue;
        }

        public void run() {
            try {
                System.out.println(getName() + " begin take");
                System.out.println(getName() + " have take " + queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
