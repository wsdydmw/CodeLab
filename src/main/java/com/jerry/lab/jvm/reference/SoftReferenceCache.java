package com.jerry.lab.jvm.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于软引用实现缓存，当内存不够时，可被方便回收掉，以避免OOM
 */
public class SoftReferenceCache<K, V> {
    private Map<K, InnerSoftReference<V>> cache; // 缓存对象池，<K, R->V>
    private ReferenceQueue<V> queue; // 引用队列，当GC执行后，软引用对象InnerSoftReference将被入队，遍历之用以从缓存池中清除失效的key
    private ReadWriteLock lock; // 读写锁

    public SoftReferenceCache() {
        cache = new HashMap<K, InnerSoftReference<V>>();
        queue = new ReferenceQueue<V>();
        lock = new ReentrantReadWriteLock(false);
    }

    public static void main(String[] args) {
        SoftReferenceCache<String, String[]> cache = new SoftReferenceCache<>();

        for (int i = 0; i < 3000; i++) {
            cache.put(String.valueOf(i), new String[1024]);
        }
    }

    /**
     * 向缓存池中添加对象
     *
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        try {
            lock.writeLock().lock();
            clearInvalidReference();
            cache.put(key, new InnerSoftReference<V>(key, value, queue));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 从缓存池中获取对象
     *
     * @param key
     * @return
     */
    public V get(K key) {
        try {
            lock.readLock().lock();
            InnerSoftReference<V> softReference = cache.get(key);
            V v = null;
            if (softReference != null)
                v = softReference.get();
            return v;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 当对象已被回收掉时，需要从缓存池中清除指向该对象的软引用
     */
    private void clearInvalidReference() {
        InnerSoftReference<V> softReference;
        while ((softReference = (InnerSoftReference) queue.poll()) != null) {
            if (softReference.get() == null) { //已被GC回收
                System.err.println(softReference.getKey() + "被回收");
                cache.remove(softReference.getKey()); //清除对应的softReference对象
            }
        }
    }

    /**
     * 缓存池中对象的个数
     *
     * @return
     */
    public int size() {
        try {
            lock.readLock().lock();
            int size = cache.size();
            System.out.println(Thread.currentThread().getName() + " 缓存池中对象的个数: " + size);
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 清空缓存池，定时worker每次计算前调用此方法可清除历史记录
     */
    public void clearCache() {
        try {
            lock.writeLock().lock();
            cache = new HashMap<K, InnerSoftReference<V>>();
            queue = new ReferenceQueue<V>();
            System.out.println(Thread.currentThread().getName() + "清空缓存池！");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 数据项，封装了软引用，便于获取对应缓存池中的key
     *
     * @param <V>
     */
    private class InnerSoftReference<V> extends SoftReference<V> {
        private K key;

        private InnerSoftReference(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }
}
