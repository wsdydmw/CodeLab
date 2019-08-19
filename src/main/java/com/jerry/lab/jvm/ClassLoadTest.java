package com.jerry.lab.jvm;

import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * 使用自定义加载器，实现类的自定义加载和热部署
 */
public class ClassLoadTest {
    public static void main(String[] args) throws Exception {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        System.out.println("step1 : get old class loader is " + oldCl);
        try {
            Class clazz1 = oldCl.loadClass("com.jerry.lab.Test");
            System.out.println("step2 : load class com.jerry.lab.Test");
        } catch (ClassNotFoundException e) {
            System.out.println("old class loader can't find com.jerry.lab.Test");
        }

        ClassLoader myCL = new MyClassLoader();
        System.out.println("step3 : set new class loader " + myCL + " to current thread");

        Class clazz2 = myCL.loadClass("com.jerry.lab.Test");
        System.out.println("step4 : load class com.jerry.lab.Test");

        Object obj2 = clazz2.newInstance();
        Method helloMethod2 = clazz2.getDeclaredMethod("hello", null);
        helloMethod2.invoke(obj2, null);

        myCL = null;
        clazz2 = null;
        obj2 = null;
        helloMethod2 = null;
        System.gc();
        System.out.println("unload classed loaded by my class loader");

        Thread.sleep(20 * 1000);//手动修改文件名
        ClassLoader newCL = new MyClassLoader();
        Class clazz3 = newCL.loadClass("com.jerry.lab.Test");
        System.out.println("step5 : reload class com.jerry.lab.Test");

        Object obj3 = clazz3.newInstance();
        Method helloMethod3 = clazz3.getDeclaredMethod("hello", null);
        helloMethod3.invoke(obj3, null);
    }
}

class MyClassLoader extends ClassLoader {
    private String classPath = "files\\jvm";

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
