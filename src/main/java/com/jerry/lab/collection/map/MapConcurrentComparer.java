package com.jerry.lab.collection.map;

import com.jerry.lab.common.Utils;
import com.jerry.lab.common.WritePercentOpsMonitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.jerry.lab.common.WritePercentOpsMonitor.Result;

/*
write%	Hashtable_READ	ConcurrentHashMap_READ	Collections$SynchronizedMap_READ	Hashtable_WRITE	ConcurrentHashMap_WRITE	Collections$SynchronizedMap_WRITE
0%	1775102	2990821	2451285	-1	-1	-1
5%	1812679	3017402	2659489	4244577	2869182	4637118
10%	1806493	3170195	2528182	4133195	3124682	4243413
15%	1770566	2698368	2633446	3894125	2101582	4479521
20%	1241907	3270604	2524082	2906809	2511088	4711154
25%	1651310	3172793	2372188	4209911	2708902	3951802
30%	1778558	3042606	2095919	4576898	3126741	3458498
 */
public class MapConcurrentComparer {
    static int DATA_INIT_SIZE = 10000;
    static int OPERATE_NUM = 100000;
    static int REPEAT_TIME = 3;
    static int MAX_WRITE_PERCENT = 30;
    static Map<Integer, Integer>[] targetObjects
            = new Map[]{new Hashtable(), new ConcurrentHashMap(), Collections.synchronizedMap(new HashMap<>())};

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
                        Integer value = map.get(key);
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




