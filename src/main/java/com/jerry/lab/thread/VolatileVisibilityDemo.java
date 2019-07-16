package com.jerry.lab.thread;

public class VolatileVisibilityDemo extends Thread {

    public static void main(String[] args) throws Exception {
        new WorkThread().start();
        System.out.println("flag is " + WorkThread.flag);
        //sleep的目的是等待线程启动完毕,也就是说进入run的无限循环体了
        Thread.sleep(100);
        WorkThread.flag = true;
        System.out.println("flag is " + WorkThread.flag);
    }
}

class WorkThread extends Thread {
    //静态变量，各线程共享
    public static volatile boolean flag = false;

    //无限循环,等待flag变为true时才跳出循环
    public void run() {
        while (!flag) {
        }
        System.out.println("thread end of run");
    }
}