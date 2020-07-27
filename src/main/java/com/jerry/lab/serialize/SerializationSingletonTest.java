package com.jerry.lab.serialize;

import java.io.*;

public class SerializationSingletonTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        SerializeUtil.serialize(Singleton.getSingleton(), "files/serialize/singleton");
        Singleton newSingleton = SerializeUtil.<Singleton>deserialize("files/serialize/singleton");
        //判断是否是同一个对象
        System.out.println(newSingleton == Singleton.getSingleton());
    }
}

class Singleton implements Serializable {
    private volatile static Singleton singleton;

    private Singleton() {
    }

    public static Singleton getSingleton() {
        if (singleton == null) {
            synchronized (Singleton.class) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }

    private Object readResolve() {
        return singleton;
    }
}