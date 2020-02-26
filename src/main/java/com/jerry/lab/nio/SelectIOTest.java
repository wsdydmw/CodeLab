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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 一个Client向多个Server发送请求，均采用Select多路复用模式
 */
public class SelectIOTest {
    final byte serverSize = 5;
    final String address = "127.0.0.1";
    final int basePort = 8880;

    public static void main(String[] args) {
        new SelectIOTest().process();
    }

    public void process() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new SelectServerSocketThread(i));
        }

        executorService.execute(new SelectSocketThread());
    }

    /**
     * Server，接受请求
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
            } catch (IOException e) {
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

        public void listen() throws IOException {
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
                            buffer.flip();
                            byte param = buffer.get();

                            if (param != order) {
                                System.out.println("order error");
                            }
                            ProcessMonitor.serverReceived(order);
                            try {
                                Thread.sleep((long) Math.random() * 1000);// server process
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            buffer.clear();
                            channel.write(buffer);
                            ProcessMonitor.serverReturn(order);
                        }
                    }
                    //需要手动移除，防止重复处理
                    keys.remove();
                }
            }
        }
    }

    /**
     * client，向多个server发送请求
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void init() {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void link(byte order) throws IOException {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(address, basePort + order));

            channel.register(selector, SelectionKey.OP_CONNECT);
        }

        public void listen() throws IOException {
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
                    } else if (key.isWritable()) {// 可写数据事件
                        SocketChannel channel = (SocketChannel) key.channel();

                        // 发送消息
                        byte order = (byte) (Integer.valueOf(StringUtils.substringAfter(channel.getRemoteAddress().toString(), ":")) - basePort);
                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        buffer.put(order);

                        buffer.clear();
                        channel.write(buffer);

                        try {
                            Thread.sleep((long) Math.random() * 1000);// network delay
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ProcessMonitor.clientSend(order);

                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) { // 有可读数据事件。
                        SocketChannel channel = (SocketChannel) key.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        channel.read(buffer);

                        buffer.flip();
                        byte order = buffer.get();
                        ProcessMonitor.clientReceived(order);
                    }
                    keys.remove();
                }
            }
        }
    }
}
