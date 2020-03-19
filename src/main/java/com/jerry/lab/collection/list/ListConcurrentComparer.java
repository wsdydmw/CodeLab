package com.jerry.lab.collection.list;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentUtils.Result;

/*
write %	ArrayList	Collections$SynchronizedRandomAccessList	CopyOnWriteArrayList
0%	2460	2694	2529
2%	2486(Unsafe)	2572	9694
4%	2527(Unsafe)	2459	332514
6%	2377(Unsafe)	3714	360131
 */
public class ListConcurrentComparer {
    static int DATA_INIT_SIZE = 1000;
    static int OPERATE_NUM = 5000000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 6;
    static List<Long>[] targetObjects
            = new List[]{new ArrayList(), Collections.synchronizedList(new ArrayList<>()), new CopyOnWriteArrayList()};

    public static void main(String[] args) {
        new ListConcurrentComparer().process();
        System.exit(0);
    }

    public static int getRandomIndex(List list) {
        return (int) (Math.random() * list.size());
    }

    public void process() {
        // step1. 增加重复次数并故意乱序执行顺序
        List<ListConcurrentComparer.Task> Tasks = new ArrayList<>();
        Stream.of(targetObjects).forEach(object -> {
            for (int writeP = 0; writeP <= MAX_WRITE_PERCENT; writeP += 2) {
                for (int i = 1; i <= REPEAT_TIME; i++) {
                    Tasks.add(new ListConcurrentComparer.Task(object, writeP));
                }
            }
        });
        Utils.shuffleList(Tasks);

        // step2. 开始运行
        System.out.println("--- begin work " + Tasks.size() + " Tasks");
        List<WritePercentUtils.Result> results = new ArrayList<>();

        Tasks.stream().forEachOrdered(task -> {
            WritePercentUtils.Result result = task.process();
            results.add(result);
            System.out.println(result);
        });

        // step3. 汇总结果
        WritePercentUtils.showResult(targetObjects, results);
    }

    private class Task {
        List<Long> list;
        int writePercent;

        public Task(List<Long> list, int writePercent) {
            this.list = list;
            this.writePercent = writePercent;
        }

        public Result process() {
            Result result = new Result();
            result.setObjectName(Utils.getClassName(list));
            result.setWritePercent(writePercent);

            // 1. init data
            list.clear();
            LongStream.range(0, DATA_INIT_SIZE).forEach(value -> {
                list.add(value);
            });

            // 2. simulate concurrent
            ExecutorService executorService = Executors.newCachedThreadPool();
            int readCount = OPERATE_NUM * (100 - writePercent) / 100;
            int writeCount = OPERATE_NUM * writePercent / 100;
            CountDownLatch readCountLatch = new CountDownLatch(readCount);
            CountDownLatch writeCountLatch = new CountDownLatch(writeCount);

            long begin = System.currentTimeMillis();
            // read thread
            new Thread(() -> {
                for (int i = 0; i < readCount; i++) {
                    executorService.submit(() -> {
                        int index = getRandomIndex(list);
                        long value = list.get(index) != null ? list.get(index) : 0L;
                        Utils.calNumber(value);
                        readCountLatch.countDown();
                    });
                }
            }).start();

            // write thread
            new Thread(() -> {
                for (int i = 0; i < writeCount; i++) {
                    executorService.submit(() -> {
                        list.add(0L);
                        writeCountLatch.countDown();
                    });
                }
            }).start();

            try {
                readCountLatch.await();
                writeCountLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.setCostTime(System.currentTimeMillis() - begin);
            list.clear();

            // 3. check result
            if (DATA_INIT_SIZE + writeCount != list.size()) {
                result.setSafe("(not thread safe, expect " + DATA_INIT_SIZE + writeCount + " but " + list.size() + ")");
            }
            return result;
        }
    }

}
