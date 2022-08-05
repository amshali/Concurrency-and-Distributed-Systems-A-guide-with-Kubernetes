package com.github.amshali.basiccache;

import java.util.Comparator;
import java.util.TreeSet;

public class LfuEvictionManager<K extends Comparable<K>> extends SmallestAttributeEvictionManager<K, Long> {
  public LfuEvictionManager() {
    keySet = new TreeSet<>(
        Comparator.comparing((K k) -> attributeMap.getOrDefault(k, 0L)).thenComparing(k -> k));
  }

  @Override
  public void access(K key) {
    keySet.remove(key);
    attributeMap.put(key, attributeMap.getOrDefault(key, 0L) + 1);
    keySet.add(key);
  }
}
