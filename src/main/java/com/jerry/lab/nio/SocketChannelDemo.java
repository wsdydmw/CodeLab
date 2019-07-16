package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SocketChannelDemo {
    // 管道管理器
    private Selector selector;

    public static void main(String[] args) throws IOException {
        new SocketChannelDemo().init("127.0.0.1", 8088).listen();
    }

    public SocketChannelDemo init(String serverIp, int port) throws IOException {
        // 获取socket通道
        SocketChannel channel = SocketChannel.open();

        channel.configureBlocking(false);
        // 获得通道管理器
        selector = Selector.open();

        // 客户端连接服务器，需要调用channel.finishConnect();才能实际完成连接。
        channel.connect(new InetSocketAddress(serverIp, port));
        // 为该通道注册SelectionKey.OP_CONNECT事件
        SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
        return this;
    }

    public void listen() throws IOException {
        System.out.println("[client] i startup");
        // 轮询访问selector
        while (true) {
            // 选择注册过的io操作的事件(第一次为SelectionKey.OP_CONNECT)
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isConnectable()) {
                    System.out.println("[client] i can connect");
                    SocketChannel channel = (SocketChannel) key.channel();

                    // 如果正在连接，则完成连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }

                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE);
                } else if (key.isWritable()) {
                    System.out.println("[client] i will send message");
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 向服务器发送消息
                    channel.write(ByteBuffer.wrap("hi, I am client".getBytes()));
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) { // 有可读数据事件。
                    System.out.println("[client] have message to read");
                    SocketChannel channel = (SocketChannel) key.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(150);
                    channel.read(buffer);

                    System.out.println("[client] received : " + new String(buffer.array()));
                }
                keys.remove();
            }
        }
    }
}
