package com.jerry.lab.collection.queue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedQueueTest {
    public static void main(String[] args) throws InterruptedException {
        DelayQueue<Item> queue = new DelayQueue<>();
        queue.put(new Item("item1", 2, TimeUnit.SECONDS));
        queue.put(new Item("item2", 4, TimeUnit.SECONDS));
        queue.put(new Item("item3", 6, TimeUnit.SECONDS));
        System.out.println("begin time : " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        while (!queue.isEmpty()) {
            Item take = queue.take();
            System.out.format("item : {%s}, time : {%s}\n", take.name, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

}

class Item implements Delayed {
    String name;
    /* 触发等待时间*/
    private long time;

    public Item(String name, long time, TimeUnit unit) {
        this.name = name;
        this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        return (this.time - ((Item)o).time) <=0 ? -1 : 1;
    }
}
