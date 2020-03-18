package com.jerry.lab.collection.map;

import com.jerry.lab.common.Utils;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * write %	HashMap	Hashtable	ConcurrentHashMap	Collections$SynchronizedMap
 * 0%	1242.0	1451.0	1377.0	1496.0
 * 2%	1372.0	1471.0	1448.0	1456.0
 * 4%	1423.0	1555.0	1364.0	1394.0
 * 6%	1355.0	1431.0	1504.0	1431.0
 * 8%	1403.0	1566.0	1944.0	1455.0
 * 10%	1307.0	1388.0	1340.0	1380.0
 */
public class HashMapConcurrentComparer {
    static int DATA_INIT_SIZE = 2000;
    static int OPERATE_NUM = 1000000;
    static int REPEAT_TIME = 1;
    static int MAX_WRITE_PERCENT = 10;
    static Map<Integer, Integer>[] targetObjects
            = new Map[]{new HashMap(), new Hashtable(), new ConcurrentHashMap(), Collections.synchronizedMap(new HashMap<>())};

    public static void main(String[] args) {
        new HashMapConcurrentComparer().process();
        System.exit(0);
    }

    public static int getRandomKey(Map map) {
        return (int) (Math.random() * map.size());
    }

    public void process() {
        // step1. 增加重复次数并故意乱序执行顺序
        List<Task> Tasks = new ArrayList<>();
        Stream.of(targetObjects).forEach(object -> {
            for (int writeP = 0; writeP <= MAX_WRITE_PERCENT; writeP += 2) {
                for (int i = 1; i <= REPEAT_TIME; i++) {
                    Tasks.add(new Task(object, writeP));
                }
            }
        });
        Utils.shuffleList(Tasks);

        // step2. 开始运行
        List<Result> results = new ArrayList<>();

        Tasks.stream().forEachOrdered(task -> {
            Result result = task.process();
            results.add(result);
            System.out.println(result);
        });

        // step3. 汇总结果
        System.out.println("---- 汇总结果 ----");
        // 标题
        System.out.print("write %\t");
        IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
            System.out.print(Utils.getClassName(targetObjects[index]) + "\t");
        });
        System.out.println();

        // 每行
        results.stream().collect(Collectors.groupingBy(Result::getWritePercent, Collectors.groupingBy(Result::getObjectName, Collectors.averagingLong(Result::getCostTime))))
                .entrySet().forEach(entry -> {
            System.out.print(entry.getKey() + "%\t");//write percent
            Map value = entry.getValue();//objectName -> costTime
            IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
                System.out.print(value.get(Utils.getClassName(targetObjects[index])) + "\t");
            });
            System.out.println();
        });
    }

    private class Task {
        Map<Integer, Integer> map;
        int writePercent;

        public Task(Map<Integer, Integer> map, int writePercent) {
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
            int readCount = OPERATE_NUM * (100 - writePercent) / 100;
            int writeCount = OPERATE_NUM * writePercent / 100;
            CountDownLatch readCountLatch = new CountDownLatch(readCount);
            CountDownLatch writeCountLatch = new CountDownLatch(writeCount);

            long begin = System.currentTimeMillis();
            // read thread
            new Thread(() -> {
                for (int i = 0; i < readCount; i++) {
                    executorService.submit(() -> {
                        int key = getRandomKey(map);
                        int value = map.containsKey(key) ? map.get(key) : 0;
                        Utils.calNumber(value);
                        readCountLatch.countDown();
                    });
                }
            }).start();

            // write thread
            new Thread(() -> {
                for (int i = 0; i < writeCount; i++) {
                    final int index = i;
                    executorService.submit(() -> {
                        map.put(DATA_INIT_SIZE + index, DATA_INIT_SIZE + index);
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

            // 3. check result
            if (DATA_INIT_SIZE + writeCount != map.size()) {
                result.setSafe(DATA_INIT_SIZE + " + " + writeCount + "!=" + map.size());
            }
            return result;
        }
    }

    @Data
    private class Result {
        String objectName;
        int writePercent;
        Long costTime;
        String safe = "Safe";
    }
}




