package com.jerry.lab.collection.queue;

import com.jerry.lab.common.QueueLengthOpsMonitor;
import com.jerry.lab.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.jerry.lab.common.QueueLengthOpsMonitor.Result;

public class BlockingQueueComparer {
    static int N = 500000;//测试数据量
    static int CONCURRENTDEGREE = 8;
    static int REPEAT_TIME = 3;

    public static void main(String[] args) throws Exception {
        new BlockingQueueComparer().process();
        System.exit(0);
    }

    public void process() {
        // step1. 增加重复次数并故意乱序执行顺序
        List<Task> Tasks = new ArrayList<>();
        for (int i = 1; i <= REPEAT_TIME; i++) {
            for (int j = 0; j <= 10; j++) {
                int length = (int) Math.pow(2d, (double) j);// queue's length
                Tasks.add(new Task(new LinkedBlockingQueue<>(length), length, false, "1:Linked"));
                Tasks.add(new Task(new ArrayBlockingQueue<>(length), length, false, "2:Array"));
                Tasks.add(new Task(new SynchronousQueue<>(false), length, false, "3:Syn-noFair"));
                Tasks.add(new Task(new SynchronousQueue<>(true), length, false, "4:Syn-Fair"));
                Tasks.add(new Task(new LinkedTransferQueue<>(), length, false, "5:Linkedtransfer"));
                Tasks.add(new Task(new LinkedBlockingQueue<>(length), length, true, "6:Linked"));
                Tasks.add(new Task(new ArrayBlockingQueue<>(length), length, true, "7:Array"));
                Tasks.add(new Task(new SynchronousQueue<>(false), length, true, "8:Syn-noFair"));
                Tasks.add(new Task(new SynchronousQueue<>(true), length, true, "9:Syn-Fair"));
                Tasks.add(new Task(new LinkedTransferQueue<>(), length, true, "10:LinkedTransfer"));
            }
        }
        Utils.shuffleList(Tasks);

        // step2. 开始运行
        System.out.println("--- begin work " + Tasks.size() + " Tasks");
        List<QueueLengthOpsMonitor.Result> results = new ArrayList<>();

        Tasks.stream().forEachOrdered(task -> {
            QueueLengthOpsMonitor.Result result = task.process();
            results.add(result);
            System.out.println(result);
        });

        // step3. 汇总结果
        QueueLengthOpsMonitor.showResult(results);
    }

    private class Task {
        BlockingQueue<Integer> queue;
        int queueLength;
        boolean isConcurrent;
        String objectName;

        public Task(BlockingQueue<Integer> queue, int queueLength, boolean isConcurrent, String objectName) {
            this.queue = queue;
            this.queueLength = queueLength;
            this.isConcurrent = isConcurrent;
            this.objectName = objectName;
        }

        public Result process() {
            Result result = new Result();
            result.setObjectName(isConcurrent ? objectName + "-C" : objectName);
            result.setQueueLength(queueLength);

            ExecutorService executorService = Executors.newFixedThreadPool(isConcurrent ? CONCURRENTDEGREE * 2 : 2);
            CountDownLatch lastPutCount = new CountDownLatch(N);
            CountDownLatch lastTakeCount = new CountDownLatch(N);

            long begin = System.nanoTime();

            for (int i = 0; i < CONCURRENTDEGREE; i++) {
                // put() threads
                executorService.execute(() -> {
                    int lastPut = 0;
                    while ((lastPut = (int) lastPutCount.getCount()) > 0) {
                        try {
                            queue.put(lastPut);
                            lastPutCount.countDown();
                        } catch (InterruptedException ex) {
                        }
                    }
                });

                // take() threads
                executorService.execute(() -> {
                    while (lastTakeCount.getCount() > 0) {
                        try {
                            queue.take();
                            lastTakeCount.countDown();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }

            try {
                lastPutCount.await();
                lastTakeCount.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long cost = System.nanoTime() - begin;
            result.setOps((long) (1000000000.0 * N / cost));// items/second

            executorService.shutdownNow();

            return result;
        }
    }
}
