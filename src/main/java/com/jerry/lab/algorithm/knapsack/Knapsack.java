package com.jerry.lab.algorithm.knapsack;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Knapsack {
    private int capacity;
    private Queue<Integer> items = new LinkedBlockingQueue<>();

    public Knapsack(int capacity) {
        this.capacity = capacity;
    }

    public void add(int item) {
        items.add(item);
    }

    public void remove(int item) {
        items.remove(item);
    }

    public int getCurrentWeight() {
        return items.stream().collect(Collectors.summingInt(Integer::valueOf));
    }

    public boolean canAdd(int item) {
        return getCurrentWeight() + item <= capacity;
    }

    public boolean isFull() {
        return getCurrentWeight() == capacity;
    }

    public String getItemsInKnapsack() {
        return StringUtils.join(items.stream().map(String::valueOf).collect(Collectors.toList()), ",");
    }
}