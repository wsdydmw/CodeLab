package com.jerry.lab.collection.list;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentUtils.Result;

/*
write%	Vector	Collections$SynchronizedRandomAccessList	CopyOnWriteArrayList
0%	709033	682984	674614
2%	433583	1049110	468939
4%	724319	1006500	325457
6%	719086	766644	353371
8%	740787	1013707	152885
10%	515329	1160628	181134
 */
public class ListConcurrentComparer {
    static int DATA_INIT_SIZE = 1000;
    static int OPERATE_NUM = 1000000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 10;
    static List<Long>[] targetObjects
            = new List[]{new Vector(), Collections.synchronizedList(new ArrayList<>()), new CopyOnWriteArrayList()};

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
            result.setOps(OPERATE_NUM * 1000 / (System.currentTimeMillis() - begin));

            // 3. check result
            if (DATA_INIT_SIZE + writeCount != list.size()) {
                result.setSafe("(not thread safe, expect " + DATA_INIT_SIZE + writeCount + " but " + list.size() + ")");
            }
            list.clear();

            return result;
        }
    }

}
