package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectTest {
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

        executorService.execute(new SelectServerSocketThread());

        for (int i = 1; i <= clientSize; i++) {
            executorService.execute(new SelectSocketThread(String.valueOf(i)));
        }

        shutDownLatch.await();
        executorService.shutdown();

        return;
    }

    class SelectServerSocketThread implements Runnable {
        // 通道管理器
        private Selector selector;

        @Override
        public void run() {
            try {
                init(8888);
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init(int port) throws IOException {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));

            // 获取通道管理器
            selector = Selector.open();

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }

        public void listen() throws IOException {
            // 使用轮询访问selector
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    // 客户端请求连接事件
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        // 获得客户端连接通道
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {// 有可读数据事件
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(100);
                        channel.read(buffer);

                        String param = new String(buffer.array());
                        ProcessMonitor.serverReceived(param);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        ByteBuffer outbuffer = ByteBuffer
                                .wrap((param).getBytes());
                        channel.write(outbuffer);
                        ProcessMonitor.serverReturn(param);
                    }
                    //需要手动移除，防止重复处理
                    keys.remove();
                }
            }
        }
    }

    class SelectSocketThread implements Runnable {

        String param;
        private Selector selector;

        public SelectSocketThread(String param) {
            this.param = param;
        }

        @Override
        public void run() {
            try {
                init(address, port);
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init(String serverIp, int port) throws IOException {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(serverIp, port));

            selector = Selector.open();
            SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
        }

        public void listen() throws IOException {
            // 轮询访问selector
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    if (key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        // 如果正在连接，则完成连接
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }

                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 向服务器发送消息
                        channel.write(ByteBuffer.wrap(param.getBytes()));
                        ProcessMonitor.clientSend(param);

                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) { // 有可读数据事件。
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(100);
                        channel.read(buffer);

                        ProcessMonitor.clientReceived(new String(buffer.array()));
                    }
                    keys.remove();
                }
            }
        }
    }
}
