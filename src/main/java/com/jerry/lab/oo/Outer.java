package com.jerry.lab.oo;

public class Outer {
    public static void main(String[] args) {
        int x = 100; //在java8里可以不加final，自动处理为final
        class Inner {
            private int y = 100;

            public int innerAdd() {
                return x + y;
            }
        }

        Inner inner = new Inner();
        System.out.println(inner.innerAdd());//200

        //x = 101; //加上这句，则java8不会将x判断为final，引起报错
    }
}