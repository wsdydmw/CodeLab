package com.jerry.lab.collection.map;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMapTest {

    public static void main(String[] args) {
        LRUCache<String, String> lruCache = new LRUCache<String, String>(5);
        lruCache.put("1", "1");
        lruCache.put("2", "2");
        lruCache.put("3", "3");
        lruCache.put("4", "4");

        lruCache.get("2");
        lruCache.put("6", "6");
        lruCache.put("5", "5");

        lruCache.get("1");
    }

}

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private LinkedHashMap<K, V> cache = null;
    private int cacheSize = 0;

    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        int hashTableCapacity = (int) Math.ceil(cacheSize / 0.75f) + 1;
        cache = new LinkedHashMap<K, V>(hashTableCapacity, 0.75f, true) {
            // (an anonymous inner class)
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean needToRemove = size() > LRUCache.this.cacheSize;
                System.out.println(needToRemove ? "need to remove " + eldest.getKey() : "not need to remove");
                return needToRemove;
            }
        };
    }

    public V put(K key, V value) {
        System.out.println("put " + key + " -> " + value);
        return cache.put(key, value);
    }

    public V get(Object key) {
        V value = cache.get(key);
        System.out.println("get " + key + " -> " + value);
        return value;
    }
}

