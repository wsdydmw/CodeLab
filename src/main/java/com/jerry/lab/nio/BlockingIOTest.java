package com.jerry.lab.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingIOTest {

    final byte serverSize = 3;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int networdDelay = 1000;//考虑到测试重点在Client，为了不产生影响，网络延迟统一在server端模拟
    final int clientProcessDelay = 2000;
    final int serverProcessDelay = 4000;
    final CountDownLatch serverLatch = new CountDownLatch(serverSize);

    public static void main(String[] args) throws InterruptedException {
        new BlockingIOTest().process();
        System.exit(0);
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ProcessMonitor.begin();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new BlockingIOTest.BlockingServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new BlockingIOTest.BlockingSocketThread());

        serverLatch.await();
        ProcessMonitor.displayProcess();
        executorService.shutdownNow();
        return;
    }

    /**
     * server端，接收请求，返回自身编号
     */
    class BlockingServerSocketThread implements Runnable {

        ServerSocket server;
        private byte order;

        public BlockingServerSocketThread(byte order) {
            this.order = order;
        }

        @Override
        public void run() {
            try {
                init();
                listen();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void init() throws IOException {
            server = new ServerSocket(basePort + order);
        }

        public void listen() throws IOException, InterruptedException {
            while (true) {
                Socket socket = server.accept();

                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 1. 接收请求
                byte param = Byte.parseByte(br.readLine());
                if (param != order) {
                    System.out.println("order error " + param + "!=" + order);
                }
                Thread.sleep(networdDelay);//模拟网络延迟
                ProcessMonitor.serverReceived(order);//阻塞

                // 2. 处理请求
                Thread.sleep(serverProcessDelay);

                // 3. 返回编号
                ProcessMonitor.serverReturn(order);
                Thread.sleep(networdDelay);
                pw.println(order);
                pw.flush();
            }
        }
    }

    /**
     * client端，串行方式向多个server发送请求
     */
    class BlockingSocketThread implements Runnable {
        Socket socket;

        @Override
        public void run() {
            try {
                for (byte i = 1; i <= serverSize; i++) {
                    link(i);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void link(byte order) throws IOException, InterruptedException {
            socket = new Socket(address, basePort + order);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 1. 发送请求
            ProcessMonitor.clientSend(order);
            pw.println(order);
            pw.flush();

            // 2. 等待结果
            ProcessMonitor.clientReceived(Byte.parseByte(br.readLine()));//阻塞

            // 3. 处理结果
            Thread.sleep(clientProcessDelay);
            ProcessMonitor.clientProcessed(order);

            serverLatch.countDown();
        }

    }
}






