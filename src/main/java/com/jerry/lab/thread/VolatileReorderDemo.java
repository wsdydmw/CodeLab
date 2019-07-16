package com.jerry.lab.thread;

public class VolatileReorderDemo {

    public static void main(String[] args) throws Exception {
        new Thread(new WriteThrea()).start();
        new Thread(new ReadThrea()).start();
    }
}

class Recorder {
    static int a = 0;
    static boolean flag = false;

    //线程A执行
    public static void write() {
        a = 1;                   //1
        flag = true;             //2
    }

    //线程B执行
    public static void read() {
        if (flag) {              //3
            int i =  a * a;      //4
        }
    }
}

class ReadThrea implements Runnable {

    @Override
    public void run() {
        Recorder.read();
    }
}

class WriteThrea implements Runnable {

    @Override
    public void run() {
        Recorder.write();
    }
}

