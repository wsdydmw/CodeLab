package com.jerry.lab.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingIOTest {

    final byte serverSize = 1;
    final String address = "127.0.0.1";
    final int basePort = 8880;

    public static void main(String[] args) {
        new BlockingIOTest().process();
    }

    public void process() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (byte i = 1; i <= serverSize; i++) {
            executorService.execute(new BlockingIOTest.BlockingServerSocketThread(i));
        }

        //executorService.execute(new BlockingIOTest.SelectSocketThread());
    }

    class BlockingServerSocketThread implements Runnable {

        ServerSocket server;
        private byte order;

        public BlockingServerSocketThread(byte order) {
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
            server = new ServerSocket(basePort + order);
        }

        public void listen() throws IOException {
            while (true) {
                try {
                    Socket socket = server.accept();
                    System.out.println("receive connection1,  address:" + server.getInetAddress() + "   port:" + server.getLocalPort());
                    System.out.println("receive connection,  address:" + socket.getInetAddress() + "   port:" + socket.getLocalPort());

                    //另起线程处理该连接的读写
                    new DisposeSocketThread(socket).start();
                    /*PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    System.out.println(1);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println(2);
                    //循环接收和发送
                    while (true) {
                        //读取消息
                        System.out.println("receive msg: ");
                        System.out.println(br.readLine());
                        System.out.println(3);

                        //输入消息
                        System.out.print("send msg:");
                        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

                        //发送消息
                        pw.println(input.readLine());
                        pw.flush();
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class DisposeSocketThread extends Thread{
        private Socket socket;

        private DisposeSocketThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                System.out.println(1);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println(2);
                //循环接收和发送

                System.out.println(br.read());
                while (br.read() != -1) {
                    //读取消息
                    System.out.println("receive msg: " + br.readLine());
                }

                    //输入消息
                    /*System.out.print("send msg:");

                    //发送消息
                    pw.println("hello");
                    pw.flush();*/
            }catch (Exception e){
                e.printStackTrace();

            }
        }
    }

    /*class BlockingSocketThread implements Runnable {

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
                        String param = new String(buffer.array()).trim();
                        ProcessMonitor.clientReceived(param);
                        shutDownLatch.countDown();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}






