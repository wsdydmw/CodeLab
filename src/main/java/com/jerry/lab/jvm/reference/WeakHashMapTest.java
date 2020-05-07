package com.jerry.lab.jvm.reference;

import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapTest {
    public static void main(String[] args) {
        // 初始化3个强引用
        String w1 = "one";
        String w2 = "two";
        String w3 = "three";

// 新建WeakHashMap
        Map<String, String> wmap = new WeakHashMap<String, String>();

// 添加键值对，此时map又添加了对w1,w2,w3的弱引用，而w4本来就只有弱引用
        wmap.put(w1, "w1");
        wmap.put(w2, "w2");
        wmap.put(w3, "w3");
        wmap.put("four", "w4");

// 打印出wmap
        System.out.printf("wmap:%s\n", wmap);

// remove(Object key) ： 删除键key对应的键值对
        wmap.remove("three");
        System.out.println("remove key three");
        System.out.printf("wmap: %s\n", wmap);

// ---- 测试 WeakHashMap 的自动回收特性 ----

// 将w1设置null，即删除强引用，只剩弱引用
        w1 = null;
        System.out.println("set w1 to null");
// 内存回收，回收WeakHashMap中与“w1”对应的键值对
        System.gc();
        System.out.println("trigger gc");

// 打印WeakHashMap
        System.out.printf("wmap:%s\n", wmap);
    }
}
