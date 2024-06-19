package com.zhiyin.plugins.utils;

import java.util.LinkedHashMap;


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
