package com.jerry.lab.collection.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityBlockingQueueTest {
    public static void main(String[] args) {
        BlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>();
        priorityQueue.add(9);
        priorityQueue.add(5);
        priorityQueue.add(2);
        priorityQueue.add(7);
        priorityQueue.add(4);

        while (priorityQueue.size() != 0) {
            System.out.print(priorityQueue.remove() + " ");
        }
    }
}
