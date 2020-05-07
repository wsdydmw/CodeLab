package com.jerry.lab.collection.list;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

public class IteratorTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        LinkedList<String> list = new LinkedList<>();
        list.add("abc");
        list.add("bbc");
        list.add("cbc");
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String str = it.next();
            System.out.println(str);
            if (str.equals("abc")) {
                it.remove();
            }
        }

        System.out.println(list.toString());
    }
}
