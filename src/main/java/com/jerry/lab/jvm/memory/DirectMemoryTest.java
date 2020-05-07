package com.jerry.lab.jvm.memory;

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

public class DirectMemoryTest {
    public static void main(String args[]) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);

        //清除
        ((DirectBuffer) byteBuffer).cleaner().clean();

    }
}
