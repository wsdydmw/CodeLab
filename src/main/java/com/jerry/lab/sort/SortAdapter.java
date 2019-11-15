package com.jerry.lab.sort;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class SortAdapter implements ISort {
    AtomicInteger count = new AtomicInteger(0);
    Integer[] _array;

    public void loadArray(Integer[] array) {
        count.set(0);
        _array = array;
    }

    public boolean isSorted(Integer[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    public void addOperateCount() {
        count.incrementAndGet();
    }

    public int getOperateCount() {
        return count.get();
    }

}
