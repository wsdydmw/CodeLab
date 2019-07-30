package com.jerry.lab.index;

public class BinarySearchArray extends IndexAdapter {
    private static final int INIT_CAPACITY = 20000;
    private Integer[] keys;
    private int N = 0;

    public BinarySearchArray() {
        keys = new Integer[INIT_CAPACITY];
    }

    private boolean isEmpty() {
        return N == 0;
    }

    /**
     * 返回当前key的数值排名
     */
    private int rank(int key) {
        int low = 0, high = N - 1;
        while (low <= high) {
            CountUtil.addOperateCount();
            int m = low + (high - low) / 2;
            if (key < keys[m]) high = m - 1;
            else if (key > keys[m]) low = m + 1;
            else return m;
        }
        return low;
    }

    /**
     * 返回插入下标，-1表示不存在
     */
    public int find(int key) {
        int i = rank(key);
        if (i < N && key == keys[i]) {
            return i;
        }

        return -1;
    }

    public void insert(int key) {
        int i = rank(key);

        // already in
        if (i < N && key == keys[i]) {
            return;
        }

        // insert new key
        for (int j = N; j > i; j--) {
            keys[j] = keys[j - 1];
            CountUtil.addOperateCount();
        }
        keys[i] = key;
        N++;
    }

    public void delete(int key) {
        if (isEmpty()) return;

        int i = rank(key);

        // key not in table
        if (i == N || key != keys[i]) {
            return;
        }

        // remove key
        for (int j = i; j < N - 1; j++) {
            CountUtil.addOperateCount();
            keys[j] = keys[j + 1];
        }
        N--;
        keys[N] = null;
    }

    public boolean check() {
        for (int i = 0; i < N - 1; i++) {
            if (keys[i] > keys[i + 1]) {
                return false;
            }
        }

        return true;
    }
}
