package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousIOTest {
    final byte serverSize = 5;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int serverProcessDelay = 2000;
    final int networkDelay = 1000;

    public static void main(String[] args) throws InterruptedException {
        new AsynchronousIOTest().process();
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new AsynchronousServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new AsynchronousSocketThread());
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
                        listen(channel);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.print("get failed. " + exc.getCause());
                }
            });

            //因为AIO不会阻塞调用进程，因此必须在主进程阻塞，才能保持进程存活。
            /*try {
                group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

        private void listen(AsynchronousSocketChannel channel) throws ExecutionException, InterruptedException {
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(1);

                buffer.clear();
                Future<Integer> future = channel.read(buffer);

                //阻塞直至数据到达
                future.get();

                buffer.flip();
                byte param = buffer.get();

                if (param != order) {
                    System.out.println("order error ");
                }
                ProcessMonitor.serverReceived(order);

                Thread.sleep((long) Math.random() * serverProcessDelay);

                buffer.clear();
                channel.write(buffer);
                ProcessMonitor.serverReturn(order);
            }

        }
    }

    /**
     * client，串行方式向多个server发送请求
     */
    class AsynchronousSocketThread implements Runnable {

        @Override
        public void run() {
            for (byte i = 1; i <= serverSize; i++) {
                try {
                    link(i);
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void link(byte order) throws InterruptedException, ExecutionException, IOException {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
            Future<Void> connect = socketChannel.connect(new InetSocketAddress(address, basePort + order));

            connect.get();

            // 发送消息
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put(order);

            buffer.clear();
            socketChannel.write(buffer);

            Thread.sleep((long) Math.random() * networkDelay);
            ProcessMonitor.clientSend(order);


            Future<Integer> read = socketChannel.read(buffer);
            read.get();

            socketChannel.read(buffer);
            buffer.flip();
            ProcessMonitor.clientReceived(buffer.get());
        }
    }
}

//
