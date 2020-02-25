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
    final int clientSize = 50;
    final CountDownLatch shutDownLatch = new CountDownLatch(clientSize);
    final String address = "127.0.0.1";
    final int port = 8888;

    public static void main(String[] args) throws InterruptedException {
        new AsynchronousIOTest().process();
        System.exit(1);
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(new AsynchronousIOTest.AsynchronousServerSocketThread());

        /*for (int i = 1; i <= clientSize; i++) {
            Thread.sleep((long) (Math.random() * 1000));
            executorService.execute(new SelectIOTest.SelectSocketThread(String.valueOf(i)));
        }*/

        shutDownLatch.await();
        executorService.shutdown();
        ProcessMonitor.displayUseTime();

        return;
    }

    class AsynchronousServerSocketThread implements Runnable {

        AsynchronousChannelGroup group;
        private AsynchronousServerSocketChannel server;

        @Override
        public void run() {
            try {
                init(port);
                listen();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void init(int port) throws IOException {
            //ChannelGroup用来管理共享资源
            group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
            server = AsynchronousServerSocketChannel.open(group);

            //通过setOption配置Socket
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            server.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);

            //绑定到指定的主机，端口
            server.bind(new InetSocketAddress(port));
            System.out.println("[server] listening on " + port);
        }

        private void listen() throws InterruptedException {
            //等待连接，并注册CompletionHandler处理连接完成后的操作。
            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                final ByteBuffer buffer = ByteBuffer.allocate(1024);

                @Override
                public void completed(AsynchronousSocketChannel channel, Object attachment) {
                    System.out.println("[server] connect is accepted");
                    buffer.clear();
                    try {
                        //将socket中的数据读取到buffer中
                        Future<Integer> future = channel.read(buffer);

                        //阻塞直至future返回
                        future.get();

                        buffer.flip();
                        System.out.println("[server] received : " + new String(buffer.array()).trim());

                        //回复客户端
                        channel.write(ByteBuffer
                                .wrap(("[server] i have received you message").getBytes()));
                        buffer.flip();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            //关闭处理完的socket，并重新调用accept等待新的连接
                            channel.close();
                            server.accept(null, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.print("[server] get failed...." + exc.getCause());
                }
            });

            //因为AIO不会阻塞调用进程，因此必须在主进程阻塞，才能保持进程存活。
            group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
    }
}

//
