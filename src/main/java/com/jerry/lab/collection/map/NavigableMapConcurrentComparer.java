package com.jerry.lab.collection.map;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentOpsMonitor;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentOpsMonitor.Result;

/*
write%	ConcurrentSkipListMap_READ	Collections$SynchronizedNavigableMap_READ	ConcurrentSkipListMap_WRITE	Collections$SynchronizedNavigableMap_WRITE
0%	25303	1938888	-1	-1
5%	13496	1772283	1142318	1325800
10%	10538	1550045	1255094	1309325
15%	10559	1220090	1333269	1159242
20%	9714	1521662	1356950	1467501
25%	7487	1410742	1338292	1440724
30%	7857	953247	1219862	1018926
 */
public class NavigableMapConcurrentComparer {
    static int DATA_INIT_SIZE = 10000;
    static int OPERATE_NUM = 100000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 30;
    static NavigableMap<Integer, Integer>[] targetObjects
            = new NavigableMap[]{new ConcurrentSkipListMap(), Collections.synchronizedNavigableMap(new TreeMap<>())};

    public static void main(String[] args) {
        new NavigableMapConcurrentComparer().process();
        System.exit(0);
    }

    public void process() {
        // step1. 增加重复次数并故意乱序执行顺序
        List<Task> Tasks = new ArrayList<>();
        Stream.of(targetObjects).forEach(object -> {
            for (int writeP = 0; writeP <= MAX_WRITE_PERCENT; writeP += 5) {
                for (int i = 1; i <= REPEAT_TIME; i++) {
                    Tasks.add(new Task(object, writeP));
                }
            }
        });
        Utils.shuffleList(Tasks);

        // step2. 开始运行
        System.out.println("--- begin work " + Tasks.size() + " Tasks");
        List<Result> results = new ArrayList<>();

        AtomicInteger order = new AtomicInteger(1);
        Tasks.stream().forEachOrdered(task -> {
            Result result = task.process();
            results.add(result);
            System.out.println(order.getAndIncrement() + " : " + result);
        });

        // step3. 汇总结果
        WritePercentOpsMonitor.showResult(targetObjects, results);
    }

    private class Task {
        NavigableMap<Integer, Integer> map;
        int writePercent;

        public Task(NavigableMap<Integer, Integer> map, int writePercent) {
            this.map = map;
            this.writePercent = writePercent;
        }

        public Result process() {
            Result result = new Result();
            result.setObjectName(Utils.getClassName(map));
            result.setWritePercent(writePercent);

            // 1. init data
            map.clear();
            IntStream.range(0, DATA_INIT_SIZE).forEach(value -> {
                map.put(value, value);
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
                if (flag == 1) {// put
                    int index = writeCount.addAndGet(1);
                    executorService.submit(() -> {
                        long begin = System.nanoTime();
                        map.put(DATA_INIT_SIZE + (int) (Math.random() * DATA_INIT_SIZE), DATA_INIT_SIZE);
                        writeNano.addAndGet(System.nanoTime() - begin);
                        operatorCountLatch.countDown();
                    });
                } else {// read
                    executorService.submit(() -> {
                        long begin = System.nanoTime();
                        int key = (int) (Math.random() * DATA_INIT_SIZE);
                        Integer value = map.ceilingKey(key);
                        if (value != null) {
                            Utils.calNumber(value);
                        }
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
            /*if (DATA_INIT_SIZE + writeCount.get() != map.size()) {
                result.setSafe(DATA_INIT_SIZE + " + " + writeCount + "!=" + map.size());
            }*/
            map.clear();

            return result;
        }
    }

}




