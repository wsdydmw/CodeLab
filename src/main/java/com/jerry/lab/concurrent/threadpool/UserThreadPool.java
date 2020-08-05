package com.jerry.lab.concurrent.threadpool;

import java.util.concurrent.*;

public class UserThreadPool {

    //线程池大小
    private static final int CORE_THREAD_SIZE = 1;
    private static final int MAX_THREAD_SIZE = 2;

    //队列大小
    private static final int QUEUE_SIZE = 2;
    //任务数量
    private static final int TASK_SIZE = 5;

    //饱和策略
    private static final int ABORT_POLICY = 0;//Abort 策略
    private static final int CALLER_RUNS_POLICY = 1;//CallerRuns 策略
    private static final int DISCARD_POLICY = 2;//Discard策略
    private static final int DISCARD_OLDEST_POLICY = 3;//DiscardOlds策略
    private static final int USER_POLICY = 4;//用户自定义策略

    public static void main(String[] args) {
        BlockingQueue queue = new LinkedBlockingQueue(QUEUE_SIZE);
        UserThreadFactory userThreadFactory = new UserThreadFactory("UserThreadFactory-1");
        RejectedExecutionHandler handler = getRejectedExecutionHandler(DISCARD_OLDEST_POLICY);

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(CORE_THREAD_SIZE, MAX_THREAD_SIZE, 60,
                        TimeUnit.SECONDS, queue, userThreadFactory, handler);

        for (int i = 0; i < TASK_SIZE; i++) {
            Runnable task = new Task(i);
            System.out.println("begin task" + i);
            threadPoolExecutor.execute(task);
        }

        threadPoolExecutor.shutdown();
    }

    private static RejectedExecutionHandler getRejectedExecutionHandler(int policy) {
        switch (policy) {
            case ABORT_POLICY:
                return new ThreadPoolExecutor.AbortPolicy();
            case CALLER_RUNS_POLICY:
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case DISCARD_POLICY:
                return new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST_POLICY:
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case USER_POLICY:
                return new UserRejectHandler();
            default:
                break;
        }

        return new ThreadPoolExecutor.AbortPolicy();
    }
}
