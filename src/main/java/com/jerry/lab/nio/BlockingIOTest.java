package com.jerry.lab.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingIOTest {

    final byte serverSize = 5;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int serverProcessDelay = 2000;
    final int networkDelay = 1000;

    public static void main(String[] args) throws InterruptedException {
        new BlockingIOTest().process();
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new BlockingIOTest.BlockingServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new BlockingIOTest.BlockingSocketThread());
    }

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

                ProcessMonitor.serverReceived(Integer.parseInt(br.readLine()));//阻塞

                Thread.sleep((long) Math.random() * serverProcessDelay);

                pw.println(order);
                pw.flush();
                ProcessMonitor.serverReturn(order);
            }
        }
    }

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

            pw.println(order);
            pw.flush();
            Thread.sleep((long) Math.random() * networkDelay);
            ProcessMonitor.clientSend(order);

            ProcessMonitor.clientReceived(Integer.parseInt(br.readLine()));//阻塞
        }

    }
}






