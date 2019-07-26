package com.jerry.lab.gc;

public class GCTimeTest {
    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) throws InterruptedException {
        byte[] allocation1, allocation2, allocation3, allocation4;
        Thread.sleep(30000);
        System.out.println("step1");
        allocation1 = new byte[10 * _1MB];

        Thread.sleep(10000);
        System.out.println("step2");
        allocation2 = new byte[20 * _1MB];
        allocation1 = null;

        Thread.sleep(10000);
        System.out.println("step3");
        allocation3 = new byte[30 * _1MB];
        allocation2 = null;

        Thread.sleep(10000);
        System.out.println("step4");
        allocation4 = new byte[40 * _1MB];
        allocation3 = null;

        /*for (int i = 0; i < 10000; i++) {
            if (map.size() * 512 / 1024 / 1024 >= 400) {
                map.clear();//保护内存不溢出
                System.out.println("clean");

                Thread.sleep(100);
            }
            byte[] b1;
            for (int j = 0; j < 100; j++) {
                b1 = new byte[512];
                map.put(System.nanoTime(), b1);//不断消耗内存
            }
        }*/
    }
}
