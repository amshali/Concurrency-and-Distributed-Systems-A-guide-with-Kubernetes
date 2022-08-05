package com.github.amshali.basiccache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class CacheImpl<K extends Comparable<K>, V> implements Cache<K, V> {
  private final Map<K, V> data;
  private final ReentrantLock lock = new ReentrantLock(true);
  private final EvictionManager<K> evictionManager;
  private final int maxSize;
  /**
   * A real number between 0 and 1.
   */
  private final float shrinkPercentage;

  public CacheImpl(EvictionManager<K> evictionManager, int maxSize, float shrinkPercentage) {
    this.evictionManager = evictionManager;
    this.maxSize = maxSize;
    this.shrinkPercentage = shrinkPercentage;
    data = new HashMap<>();
  }

  @Override
  public V get(K key) {
    lock.lock();
    try {
      var cached = data.get(key);
      if (cached == null) {
        return null;
      }
      evictionManager.access(key);
      return cached;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void set(K key, V value) {
    lock.lock();
    try {
      data.put(key, value);
      evictionManager.access(key);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(K key) {
    lock.lock();
    try {
      data.remove(key);
      evictionManager.delete(key);
    } finally {
      lock.unlock();
    }
  }

  public void evict() {
    lock.lock();
    try {
      final var numItemsToEvict = Math.ceil(maxSize * shrinkPercentage);
      while (maxSize - data.size() < numItemsToEvict) {
        var k = evictionManager.evict();
        data.remove(k);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return data.size();
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    CacheImpl<String, String> c = new CacheImpl<>(new LfuEvictionManager<String>(), 4, 0.2f);
    c.set("n", "amin");
    c.set("b", "bob");
    c.set("t", "cat");
    c.set("a", "dan");
    c.set("e", "emma");
    c.get("e");
    c.get("e");
    c.get("e");
    c.get("t");
    c.get("t");
    c.get("t");
    System.out.println(c.data);
    Thread.sleep(10);
    c.get("a");
    c.get("n");
    System.out.println(c.data);
    Thread.sleep(10);
    System.out.println(c.data);
    var v = c.get("b");
    c.get("b");
    System.out.println(c.data);
    c.evict();
    System.out.println(c.data);
  }
}
