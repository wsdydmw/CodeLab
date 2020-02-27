package com.jerry.lab.nio;

import org.apache.commons.lang3.StringUtils;

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

/**
 * 一个Client向多个Server发送请求，均采用Select多路复用模式
 */
public class SelectIOTest {
    final byte serverSize = 3;
    final String address = "127.0.0.1";
    final int basePort = 8880;
    final int clientProcessDelay = 3000;
    final int serverProcessDelay = 4000;
    final CountDownLatch serverLatch = new CountDownLatch(serverSize);
    final CountDownLatch clientLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        new SelectIOTest().process();
    }

    public void process() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new SelectServerSocketThread(i));
        }

        Thread.sleep(3000);// 等待server启动
        executorService.execute(new SelectSocketThread());

        clientLatch.await();
        ProcessMonitor.displayProcess();
        executorService.shutdown();
        return;
    }

    /**
     * Server，接受请求，返回自身编号
     */
    class SelectServerSocketThread implements Runnable {
        private Selector selector;
        private byte order;

        public SelectServerSocketThread(byte order) {
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
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(basePort + order));

            selector = Selector.open();

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }

        public void listen() throws IOException, InterruptedException {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    // 客户端请求连接事件
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);

                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {// 可读数据事件
                        SocketChannel channel = (SocketChannel) key.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        if (channel.read(buffer) != -1) {
                            // 1. 接收请求
                            buffer.flip();
                            byte param = buffer.get();

                            if (param != order) {
                                System.out.println("order error");
                            }
                            ProcessMonitor.serverReceived(order);

                            // 2. 处理请求
                            Thread.sleep(serverProcessDelay);

                            // 3. 返回编号
                            buffer.clear();
                            channel.write(buffer);
                            ProcessMonitor.serverReturn(order);

                            // 4. 关闭Server
                            serverLatch.countDown();
                            return;
                        }
                    }
                    //需要手动移除，防止重复处理
                    keys.remove();
                }
            }
        }
    }

    /**
     * client，串行方式向多个server发送请求
     */
    class SelectSocketThread implements Runnable {

        private Selector selector;

        @Override
        public void run() {
            try {
                init();
                for (byte i = 1; i <= serverSize; i++) {
                    link(i);
                }
                listen();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void init() throws IOException {
            selector = Selector.open();
        }

        public void link(byte order) throws IOException {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(address, basePort + order));

            channel.register(selector, SelectionKey.OP_CONNECT);
        }

        public void listen() throws IOException, InterruptedException {
            while (serverLatch.getCount() != 0) {
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
                    } else if (key.isWritable()) {// 可写数据事件
                        // 1. 发送请求
                        SocketChannel channel = (SocketChannel) key.channel();

                        byte order = (byte) (Integer.valueOf(StringUtils.substringAfter(channel.getRemoteAddress().toString(), ":")) - basePort);
                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        buffer.put(order);

                        buffer.clear();
                        channel.write(buffer);
                        ProcessMonitor.clientSend(order);

                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) { // 有可读数据事件。
                        // 2. 等到结果
                        SocketChannel channel = (SocketChannel) key.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        channel.read(buffer);

                        buffer.flip();
                        byte order = buffer.get();
                        ProcessMonitor.clientReceived(order);

                        // 3. 处理结果
                        Thread.sleep(clientProcessDelay);
                        ProcessMonitor.clientProcessed(order);
                    }
                    keys.remove();
                }
            }
            clientLatch.countDown();
            return;
        }
    }
}
