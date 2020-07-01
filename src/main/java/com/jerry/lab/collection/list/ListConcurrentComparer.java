package com.jerry.lab.collection.list;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentOpsMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentOpsMonitor.Result;

/*
write%	Vector_READ	Collections$SynchronizedRandomAccessList_READ	CopyOnWriteArrayList_READ	Vector_WRITE	Collections$SynchronizedRandomAccessList_WRITE	CopyOnWriteArrayList_WRITE
0%	1877370	4000096	4897791	-1	-1	-1
2%	1319563	2385534	6577924	210632	211881	47148
4%	144834	594788	7398435	61293	105870	2008
6%	15509	78187	7377349	13126	37628	9
8%	588	1207	8988544	610	1147	2
10%	81	128	7732372	84	128	0
 */
public class ListConcurrentComparer {
    static int DATA_INIT_SIZE = 1000;
    static int OPERATE_NUM = 1000 * 1000;
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
        List<WritePercentOpsMonitor.Result> results = new ArrayList<>();

        AtomicInteger order = new AtomicInteger(1);
        Tasks.stream().forEachOrdered(task -> {
            WritePercentOpsMonitor.Result result = task.process();
            results.add(result);
            System.out.println(order.getAndIncrement() + " : " + result);
        });

        // step3. 汇总结果
        WritePercentOpsMonitor.showResult(targetObjects, results);
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
            AtomicInteger writeCount = new AtomicInteger(0);
            CountDownLatch operatorCountLatch = new CountDownLatch(OPERATE_NUM);
            AtomicLong readNano = new AtomicLong(0);
            AtomicLong writeNano = new AtomicLong(0);

            Stream.generate(() -> {
                if (Math.random() * 100 < writePercent) {
                    return 1;
                } else {
                    return 0;
                }
            }).limit(OPERATE_NUM).forEach(flag -> {
                if (flag == 1) {// add
                    executorService.submit(() -> {
                        long begin = System.nanoTime();
                        list.add((int) (Math.random() * DATA_INIT_SIZE), 0L);
                        writeCount.addAndGet(1);
                        writeNano.addAndGet(System.nanoTime() - begin);
                        operatorCountLatch.countDown();
                    });
                } else {// read
                    executorService.submit(() -> {
                        long begin = System.nanoTime();
                        int index = getRandomIndex(list);
                        long value = list.get(index) != null ? list.get(index) : 0L;
                        Utils.calNumber(value);
                        readNano.addAndGet(System.nanoTime() - begin);
                        operatorCountLatch.countDown();
                    });
                }
            });

            try {
                operatorCountLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double readOps = OPERATE_NUM;
            readOps *= (double) (100 - writePercent) / 100;
            readOps /= (double) readNano.get() / 1000000000;
            result.setReadOps((long) readOps);
            if (writePercent != 0) {
                double writeOps = OPERATE_NUM;
                writeOps *= (double) writePercent / 100;
                writeOps /= (double) writeNano.get() / 1000000000;
                result.setWriteOps((long) writeOps);
            }

            // 3. check result
            if (DATA_INIT_SIZE + writeCount.get() != list.size()) {
                result.setSafe("(not thread safe, expect " + DATA_INIT_SIZE + writeCount + " but " + list.size() + ")");
            }
            list.clear();

            return result;
        }
    }

}
