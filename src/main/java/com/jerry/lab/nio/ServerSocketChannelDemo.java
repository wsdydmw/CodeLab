package com.jerry.lab.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerSocketChannelDemo {
    // 通道管理器
    private Selector selector;

    public static void main(String[] args) throws IOException {
        new ServerSocketChannelDemo().init(8088).listen();
    }

    // 获取一个ServerSocket通道，并初始化通道
    public ServerSocketChannelDemo init(int port) throws IOException {
        // 获取一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));

        // 获取通道管理器
        selector = Selector.open();

        // 将通道管理器与通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，
        // 只有当该事件到达时，Selector.select()会返回，否则一直阻塞。
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        return this;
    }

    public void listen() throws IOException {
        System.out.println("[server] i am startup");

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
                    System.out.println("[server] connect is accepted");
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获得客户端连接通道
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {// 有可读数据事件
                    System.out.println("[server] have message to read");
                    // 获取客户端传输数据可读取消息通道。
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 创建读取数据缓冲器
                    ByteBuffer buffer = ByteBuffer.allocate(100);

                    if (channel.read(buffer) != -1) {
                        buffer.flip();

                        // 读取缓冲区中数据
                        System.out.println("[server] received : " + new String(buffer.array()));

                        // 清空缓冲区，把position设为0，把limit设为capacity，一般在把数据写入Buffer前调用。
                        buffer.clear();

                        ByteBuffer outbuffer = ByteBuffer
                                .wrap(("[server] i have received you message").getBytes());
                        channel.write(outbuffer);
                    }
                }
                //需要手动移除，防止重复处理
                keys.remove();
            }
        }
    }
}
