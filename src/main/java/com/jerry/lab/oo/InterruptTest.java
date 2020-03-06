package com.jerry.lab.oo;

public class InterruptTest {

    public static void main(String[] args) {
        Thread t = new Thread(new InterruptableThread());
        t.start();
        System.out.println("1. in main() - begin start thread " + t.getName());
        // 主线程休眠2秒，从而确保刚才启动的线程有机会执行一段时间
        try {
            Thread.sleep(2000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("3. in main() - interrupting the thread " + t.getName());
        t.interrupt();// 中断线程，导致其抛出异常
        System.out.println("4. in main() - leaving");
    }
}

class InterruptableThread implements Runnable{

    public void run(){
        while (!Thread.currentThread().interrupted()) {
            try {

                    System.out.println("2. in run() - "
                            + Thread.currentThread().getName()
                            + " going to sleep 20 second, now isInterrupted() is "
                            + Thread.currentThread().isInterrupted());//false
                    Thread.sleep(5000);
                    System.out.println("in run() - "
                            + Thread.currentThread().getName()
                            + "woke up");

            } catch (InterruptedException e) {
                System.out.println("5. in run() - get InterruptedException， now isInterrupted() is "
                        + Thread.currentThread().isInterrupted());// false，并不是true，抛出异常的时候“是否请求中断标志”被重置了
                Thread.currentThread().interrupt();// 并不会立即中断线程，仅仅修改了“是否请求中断标志”为true，如果想停止线程，可在代码中通过检查该标志，自行实现停止逻辑
                System.out.println("6. in run() - execute Thread.currentThread().interrupt(), now isInterrupted() is "
                        + Thread.currentThread().isInterrupted());
            }
        }
    }
}
