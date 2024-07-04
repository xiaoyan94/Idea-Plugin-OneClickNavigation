package com.zhiyin.plugins.utils;

import java.util.LinkedHashMap;

/**
 * LRU Cache 是 LinkedHashMap 的子类，实现了一个 LRU 策略的缓存，当缓存满时，会自动删除最老的缓存项。
 */
public final class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_SIZE;

    public LRUCache(int maxSize) {
        super(16, 0.75f, true);
        this.MAX_SIZE = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > MAX_SIZE;
    }
}
