package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(new ServerSocketThread());
        Thread.sleep(3000);
        executorService.execute(new SocketThread());
    }
}

class ServerSocketThread implements Runnable {
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
            // 当有注册的事件到达时，方法返回，否则阻塞。
            selector.select();

            // 获取selector中的迭代器，选中项为注册的事件
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
                    System.out.println("[server " + LocalDateTime.now() + "] client is readable [got call]");

                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(100);
                    channel.read(buffer);

                    System.out.println("[server " + LocalDateTime.now() + "] [begin prepare data]");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("[server " + LocalDateTime.now() + "] [end prepare data]");

                    ByteBuffer outbuffer = ByteBuffer
                            .wrap(("this is result").getBytes());
                    channel.write(outbuffer);
                    System.out.println("[server " + LocalDateTime.now() + "] [return readable]");

                    /*if (channel.read(buffer) != -1) {
                        buffer.flip();

                        // 读取缓冲区中数据
                        System.out.println("[server " + LocalDateTime.now() + "] [got call]");
                        // 清空缓冲区，把position设为0，把limit设为capacity，一般在把数据写入Buffer前调用。
                        buffer.clear();

                        System.out.println("[server " + LocalDateTime.now() + "] [begin prepare data]");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("[server " + LocalDateTime.now() + "] [end prepare data]");

                        ByteBuffer outbuffer = ByteBuffer
                                .wrap(("[server " + LocalDateTime.now() + "] this is result").getBytes());
                        channel.write(outbuffer);
                        System.out.println("[server " + LocalDateTime.now() + "] [return call]");
                    }*/
                }
                //需要手动移除，防止重复处理
                keys.remove();
            }
        }
    }
}

class SocketThread implements Runnable {
    private Selector selector;

    @Override
    public void run() {
        try {
            init("127.0.0.1", 8888);
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(String serverIp, int port) throws IOException {
        // 获取socket通道
        SocketChannel channel = SocketChannel.open();

        channel.configureBlocking(false);
        // 获得通道管理器
        selector = Selector.open();

        // 客户端连接服务器，需要调用channel.finishConnect();才能实际完成连接。
        channel.connect(new InetSocketAddress(serverIp, port));
        // 为该通道注册SelectionKey.OP_CONNECT事件
        SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void listen() throws IOException {
        // 轮询访问selector
        while (true) {
            // 选择注册过的io操作的事件(第一次为SelectionKey.OP_CONNECT)
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
                    channel.write(ByteBuffer.wrap("hi, I am client, plz give me you name".getBytes()));
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) { // 有可读数据事件。
                    System.out.println("[client " + LocalDateTime.now() + "] server is readable [begin get result]");

                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(150);
                    channel.read(buffer);

                    System.out.println("[client " + LocalDateTime.now() + "] [end get result]");
                }
                keys.remove();
            }
        }
    }
}
