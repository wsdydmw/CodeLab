package com.jerry.lab;

public class Test {
    public void hello() {
        System.out.println("i am loaded by " + getClass().getClassLoader().getClass());
    }
}
