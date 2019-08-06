package com.jerry.lab.jvm;

import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * 使用Thread.setContextClassLoader()切换自定义加载器
 */
public class ClassLoadTest {
    public static void main(String[] args) throws Exception {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        System.out.println("old class loader is " + oldCl);
        try {
            Class clazz1 = oldCl.loadClass("com.jerry.lab.Test");
        } catch (ClassNotFoundException e) {
            System.out.println("old class loader can't find com.jerry.lab.Test");
        }

        ClassLoader newCl = new MyClassLoader("F:\\IntelliJ Workspace\\Lab\\JustTest\\external");
        System.out.println("new class loader is " + newCl);
        Thread.currentThread().setContextClassLoader(newCl);
        Class clazz2 = newCl.loadClass("com.jerry.lab.Test");
        Object obj = clazz2.newInstance();
        Method helloMethod = clazz2.getDeclaredMethod("hello", null);
        helloMethod.invoke(obj, null);
    }
}

class MyClassLoader extends ClassLoader {
    private String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    private byte[] loadByte(String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        FileInputStream fis = new FileInputStream(classPath + "/" + name
                + ".class");
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

}
