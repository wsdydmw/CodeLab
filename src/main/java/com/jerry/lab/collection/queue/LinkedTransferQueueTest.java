package com.jerry.lab.collection.queue;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class LinkedTransferQueueTest {
    public static void main(String[] args) {
        LinkedTransferQueue<Integer> queue = new LinkedTransferQueue<Integer>();
        Producer p1 = new Producer("p1", queue, 10);
        Producer p2 = new Producer("p2", queue, 20);
        Producer p3 = new Producer("p3", queue, 30);
        Producer p4 = new Producer("p4", queue, 40);
        Producer p5 = new Producer("p5", queue, 50);

        Consumer c1 = new Consumer("c1", queue);
        Consumer c2 = new Consumer("c2", queue);
        Consumer c3 = new Consumer("c3", queue);
        Consumer c4 = new Consumer("c4", queue);
        Consumer c5 = new Consumer("c3", queue);

        Object[] objects = new Object[]{c1, c2, p1, c3, p2, p3, p4, p5, c4, c5};

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
        private LinkedTransferQueue<Integer> queue;
        private int n;

        public Producer(String name, LinkedTransferQueue<Integer> queue, int n) {
            super(name);
            this.queue = queue;
            this.n = n;
        }

        public void run() {
            System.out.println(getName() + " begin");
            long begin = System.currentTimeMillis();
            queue.put(n);
            System.out.println(getName() + " have putted " + n + " cost " + (System.currentTimeMillis() - begin) + "ms");
        }
    }

    static class Consumer extends Thread {
        private LinkedTransferQueue<Integer> queue;

        public Consumer(String name, LinkedTransferQueue<Integer> queue) {
            super(name);
            this.queue = queue;
        }

        public void run() {
            System.out.println(getName() + " begin");
            long begin = System.currentTimeMillis();
            try {
                System.out.println(getName() + " have take " + queue.take() + " cost " + (System.currentTimeMillis() - begin) + "ms");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
