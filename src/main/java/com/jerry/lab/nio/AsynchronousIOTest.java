package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.*;

public class AsynchronousIOTest {
    final byte serverSize = 3;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int networdDelay = 1000;//考虑到测试重点在Client，为了不产生影响，网络延迟统一在server端模拟
    final int clientProcessDelay = 2000;
    final int serverProcessDelay = 4000;
    final CountDownLatch serverLatch = new CountDownLatch(serverSize);

    public static void main(String[] args) throws InterruptedException {
        new AsynchronousIOTest().process();
        System.exit(0);
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ProcessMonitor.begin();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new AsynchronousServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new AsynchronousSocketThread());

        serverLatch.await();
        ProcessMonitor.displayProcess();
        executorService.shutdownNow();
        return;
    }

    /**
     * Server，接受请求，返回自身编号
     */
    class AsynchronousServerSocketThread implements Runnable {

        private AsynchronousChannelGroup group;
        private AsynchronousServerSocketChannel server;
        private byte order;

        public AsynchronousServerSocketThread(byte order) {
            this.order = order;
        }

        @Override
        public void run() {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void init() throws IOException {
            //ChannelGroup用来管理共享资源
            group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
            server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(basePort + order));

            //通过setOption配置Socket
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            server.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);

            //等待连接，并注册CompletionHandler处理连接完成后的操作。
            server.accept("first connect", new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel channel, Object attachment) {
                    try {
                        handleConnect(channel);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.print("get failed. " + exc.getCause());
                }
            });
        }

        private void handleConnect(AsynchronousSocketChannel channel) throws ExecutionException, InterruptedException {
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(1);

                // 1. 接收请求
                buffer.clear();
                Future<Integer> future = channel.read(buffer);
                future.get();//阻塞

                buffer.flip();
                byte param = buffer.get();
                if (param != order) {
                    System.out.println("order error ");
                }
                Thread.sleep(networdDelay);//模拟网络延迟
                ProcessMonitor.serverReceived(order);

                // 2. 处理请求
                Thread.sleep(serverProcessDelay);

                // 3. 返回编号
                ProcessMonitor.serverReturn(order);
                Thread.sleep(networdDelay);
                buffer.clear();
                channel.write(buffer);
            }

        }
    }

    /**
     * client，串行方式向多个server发送请求
     */
    class AsynchronousSocketThread implements Runnable {

        private ExecutorService executor;

        @Override
        public void run() {
            init();
            for (byte i = 1; i <= serverSize; i++) {
                try {
                    link(i);
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void init() {
            executor = Executors.newCachedThreadPool();
        }

        private void link(byte order) throws InterruptedException, ExecutionException, IOException {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
            Future<Void> connect = socketChannel.connect(new InetSocketAddress(address, basePort + order));
            connect.get();//阻塞

            //连接之后，执行send
            FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    // 1. 发送请求
                    ByteBuffer buffer = ByteBuffer.allocate(1);
                    buffer.put(order);

                    buffer.clear();
                    socketChannel.write(buffer);
                    ProcessMonitor.clientSend(order);

                    // 2. 等待结果
                    buffer.clear();
                    Future<Integer> read = socketChannel.read(buffer);
                    read.get();//阻塞

                    socketChannel.read(buffer);
                    buffer.flip();
                    ProcessMonitor.clientReceived(buffer.get());

                    // 3. 处理结果
                    Thread.sleep(clientProcessDelay);
                    ProcessMonitor.clientProcessed(order);

                    serverLatch.countDown();

                    return 0;
                }
            });

            executor.submit(futureTask);
        }

    }
}

//
