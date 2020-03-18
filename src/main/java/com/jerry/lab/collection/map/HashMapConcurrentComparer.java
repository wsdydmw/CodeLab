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

public class HashMapConcurrentComparer {
    static int DATA_INIT_SIZE = 1000;
    static int OPERATE_NUM = 500000;
    static int repeatTime = 2;

    public static void main(String[] args) {
        // step1. 准备待测试对象
        Map<Integer, Integer>[] targetObjects = new Map[]{new HashMap(), new Hashtable(), new ConcurrentHashMap(), Collections.synchronizedMap(new HashMap<>())};

        // step2. 增加重复次数并故意乱序执行顺序
        List<Map<Integer, Integer>> testObjects = new ArrayList<>();
        Stream.of(targetObjects).forEach(object -> {
            for (int i = 1; i <= repeatTime; i++) {
                testObjects.add(object);
            }
        });
        Utils.shuffleList(testObjects);

        // step3. 开始运行
        Map<Integer, Map> avgResult = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            int writePercent = 0 + 2 * i;
            avgResult.put(writePercent, new HashMapConcurrentComparer().doTestWithDiffWP(testObjects, writePercent));
        }

        // step4. 汇总结果
        System.out.println("---- 汇总结果 ----");
        // 标题
        System.out.print("write %\t");
        IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
            System.out.print(Utils.getClassName(targetObjects[index]) + "\t");
        });
        System.out.println();

        // 每行
        avgResult.entrySet().stream().forEachOrdered(entry -> {
            System.out.print(entry.getKey() + "%\t");
            Map value = entry.getValue();
            IntStream.range(0, targetObjects.length).forEachOrdered(index -> {
                System.out.print(value.get(Utils.getClassName(targetObjects[index])) + "\t");
            });
            System.out.println();
        });


        System.exit(0);
    }

    public static int getRandomKey(Map map) {
        return (int) (Math.random() * map.size());
    }

    public Map doTestWithDiffWP(List<Map<Integer, Integer>> testObjects, int writePercent) {
        List<Result> results = new ArrayList<>();

        // step1. 加入计算结果
        testObjects.stream().forEachOrdered(object -> {
            Result result = doTestWithDiffObjectAndWP(object, writePercent);
            results.add(result);
            System.out.println(result);
        });

        // step2. 计算结果分析
        return results.stream().collect(Collectors.groupingBy(Result::getObjectName, Collectors.averagingLong(Result::getCostTime)));
    }

    public Result doTestWithDiffObjectAndWP(Map<Integer, Integer> map, int writePercent) {
        Result result = new Result();
        result.setObjectName(Utils.getClassName(map));

        // 1. init data
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
            result.setSafe(false);
        }
        return result;
    }

    @Data
    class Result {
        String objectName;
        Long costTime;
        boolean safe = true;
    }

}


