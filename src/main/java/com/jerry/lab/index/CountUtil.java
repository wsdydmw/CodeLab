package com.jerry.lab.index;

import java.util.concurrent.atomic.AtomicInteger;

public class CountUtil {
    static AtomicInteger count = new AtomicInteger(0);

    public static void initOperateCount() {
        count.set(0);
    }

    public static void addOperateCount() {
        count.incrementAndGet();
    }

    public static int getOperateCount() {
        return count.get();
    }

}
