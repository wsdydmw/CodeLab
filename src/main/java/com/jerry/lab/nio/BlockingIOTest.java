package com.jerry.lab.nio;

import sun.java2d.SurfaceDataProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingIOTest {

    final int clientSize = 5;
    final CountDownLatch shutDownLatch = new CountDownLatch(clientSize);
    final String address = "127.0.0.1";
    final int port = 8888;

    public static void main(String[] args) throws InterruptedException {
        new BlockingIOTest().process();
        System.exit(1);
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(new BlockingServerSocketThread());

        for (int i = 1; i <= clientSize; i++) {
            executorService.execute(new BlockingSocketThread(String.valueOf(i)));
        }

        shutDownLatch.await();
        executorService.shutdown();

        return;
    }

    class BlockingServerSocketThread implements Runnable {

        ServerSocketChannel serverChannel;

        @Override
        public void run() {
            try {
                init(port);
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init(int port) throws IOException {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
        }

        public void listen() throws IOException {
            while (shutDownLatch.getCount()!=0) {
                SocketChannel socketChannel = serverChannel.accept();
                ByteBuffer buffer = ByteBuffer.allocate(100);
                socketChannel.read(buffer);

                String param = new String(buffer.array());

                ProcessMonitor.serverReceived(param);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketChannel.write(ByteBuffer.wrap(param.getBytes()));
                ProcessMonitor.serverReturn(param);
            }
        }
    }

    class BlockingSocketThread implements Runnable {

        SocketChannel channel;
        String param;

        public BlockingSocketThread(String param) {
            this.param = param;
        }

        @Override
        public void run() {
            try {
                init(address, port);
                send();
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init(String serverIp, int port) throws IOException {
            // 获取socket通道
            channel = SocketChannel.open(new InetSocketAddress(serverIp, port));
        }

        public void send() {
            try {
                channel.write(ByteBuffer.wrap(param.getBytes()));
                ProcessMonitor.clientSend(param);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void listen() {
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(100);
                try {
                    if (channel.read(buffer) != -1) {
                        ProcessMonitor.clientReceived(new String(buffer.array()));
                        shutDownLatch.countDown();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}






