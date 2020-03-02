package com.jerry.lab.oo;

public class ExtendDemo {
    public static void main(String[] args) {
        Base base = new Sub();
        System.out.println(base.name);
        System.out.println(Base.staticName);
        base.print();
        Base.staticPrint();
    }
}

class Base {
    static String staticName = "父类静态变量";
    String name = "父类变量";

    public static void staticPrint() {
        System.out.println("父类静态方法");
    }

    public void print() {
        System.out.println("父类方法");
    }
}

class Sub extends Base {
    static String staticName = "子类静态变量";
    String name = "子类变量";

    public static void staticPrint() {
        System.out.println("子类静态方法");
    }

    public void print() {
        System.out.println("子类方法");
    }
}
