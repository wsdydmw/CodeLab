package com.jerry.lab.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingIOTest {

    final byte serverSize = 1;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int clientProcessDelay = 3000;
    final int serverProcessDelay = 4000;
    final CountDownLatch serverLatch = new CountDownLatch(serverSize);
    final CountDownLatch clientLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        new BlockingIOTest().process();
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ProcessMonitor.begin();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new BlockingIOTest.BlockingServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new BlockingIOTest.BlockingSocketThread());

        clientLatch.await();
        ProcessMonitor.displayProcess();
        executorService.shutdown();
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
                ProcessMonitor.serverReceived(order);//阻塞

                // 2. 处理请求
                Thread.sleep(serverProcessDelay);

                // 3. 返回编号
                pw.println(order);
                pw.flush();
                ProcessMonitor.serverReturn(order);

                // 4. 关闭Server
                serverLatch.countDown();
                return;
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

                serverLatch.await();
                clientLatch.countDown();
                return;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void link(byte order) throws IOException, InterruptedException {
            socket = new Socket(address, basePort + order);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 1. 发送请求
            pw.println(order);
            pw.flush();
            ProcessMonitor.clientSend(order);

            // 2. 等到结果
            ProcessMonitor.clientReceived(Byte.parseByte(br.readLine()));//阻塞

            // 3. 处理结果
            Thread.sleep(clientProcessDelay);
            ProcessMonitor.clientProcessed(order);
        }

    }
}






