package com.jerry.lab.collection.map;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentOpsMonitor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentOpsMonitor.Result;

/*
write%	Hashtable_READ	ConcurrentHashMap_READ	Collections$SynchronizedMap_READ	ConcurrentSkipListMap_READ	Collections$SynchronizedSortedMap_READ	Hashtable_WRITE	ConcurrentHashMap_WRITE	Collections$SynchronizedMap_WRITE	ConcurrentSkipListMap_WRITE	Collections$SynchronizedSortedMap_WRITE
0%	1669527	3240014	1958811	29428	1145460	-1	-1	-1	-1	-1
5%	1707946	3360121	2433837	16051	730875	3965453	3468709	5006311	1429291	774535
10%	1140199	2834915	2731371	10282	814292	3121670	3414746	4980556	1321310	875034
15%	1426247	3106228	1874517	8778	819929	4193534	3138883	3216330	1401212	1116225
20%	1135111	3214589	2566377	7856	616786	2148809	3629418	4657631	1525140	704480
25%	1733318	2798588	2305489	8941	502855	4492414	2289953	3711970	1581737	610528
30%	1693090	3257639	2504316	10105	1217446	4599369	2951740	4883199	735115	1416128
 */
public class MapConcurrentComparer {
    static int DATA_INIT_SIZE = 10000;
    static int OPERATE_NUM = 100000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 30;
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
                        int key = getRandomKey(map);
                        int value = map.containsKey(key) ? map.get(key) : 0;
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
            if (DATA_INIT_SIZE + writeCount.get() != map.size()) {
                result.setSafe(DATA_INIT_SIZE + " + " + writeCount + "!=" + map.size());
            }
            map.clear();

            return result;
        }
    }

}




