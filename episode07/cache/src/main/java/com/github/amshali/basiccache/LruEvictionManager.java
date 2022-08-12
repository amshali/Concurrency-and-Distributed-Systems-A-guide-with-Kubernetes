package com.github.amshali.basiccache;

import java.util.Comparator;
import java.util.TreeSet;

public class LruEvictionManager<K extends Comparable<K>> extends SmallestAttributeEvictionManager<K, Long> {

  public LruEvictionManager() {
    keySet = new TreeSet<>(
        Comparator.comparing((K k) -> attributeMap.getOrDefault(k, 0L)).thenComparing(k -> k));
  }

  public void access(K key) {
    keySet.remove(key);
    attributeMap.put(key, System.currentTimeMillis());
    keySet.add(key);
  }
}
