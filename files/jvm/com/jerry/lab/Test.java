package com.jerry.lab;

public class Test {
    public void hello() {
        System.out.println("i am the most concurrent version class loaded by " + getClass().getClassLoader().getClass());
    }
}
