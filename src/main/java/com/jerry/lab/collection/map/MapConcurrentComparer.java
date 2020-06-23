package com.jerry.lab.collection.map;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentOpsMonitor;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentOpsMonitor.Result;

/*
write%	Hashtable	ConcurrentHashMap	Collections$SynchronizedMap	ConcurrentSkipListMap	Collections$SynchronizedSortedMap
0%	663756	777531	737703	159550	663756
2%	778623	771428	722158	87796	710907
4%	669988	810572	639092	78435	710907
6%	741168	737493	686487	56046	658594
8%	714285	729282	718436	39440	585638
10%	655396	747008	745007	37512	688176
 */
public class MapConcurrentComparer {
    static int DATA_INIT_SIZE = 5000;
    static int OPERATE_NUM = 100000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 10;
    static Map<Integer, Integer>[] targetObjects
            = new Map[]{new Hashtable(), new ConcurrentHashMap(), Collections.synchronizedMap(new HashMap<>()),
            new ConcurrentSkipListMap(), Collections.synchronizedSortedMap(new TreeMap<>())};

    public static void main(String[] args) {
        new MapConcurrentComparer().process();
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
        System.out.println("--- begin work " + Tasks.size() + " Tasks");
        List<Result> results = new ArrayList<>();

        Tasks.stream().forEachOrdered(task -> {
            Result result = task.process();
            results.add(result);
            System.out.println(result);
        });

        // step3. 汇总结果
        WritePercentOpsMonitor.showResult(targetObjects, results);
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
            //result.setOps(OPERATE_NUM * 1000 / (System.currentTimeMillis() - begin));

            // 3. check result
            if (DATA_INIT_SIZE + writeCount != map.size()) {
                result.setSafe(DATA_INIT_SIZE + " + " + writeCount + "!=" + map.size());
            }
            map.clear();

            return result;
        }
    }

}




