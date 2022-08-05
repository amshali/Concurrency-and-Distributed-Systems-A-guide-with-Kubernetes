package com.github.amshali.basiccache;

import java.util.*;

public abstract class SmallestAttributeEvictionManager<K extends Comparable<K>,
    A extends Comparable<A>> implements EvictionManager<K> {

  protected final Map<K, A> attributeMap = new HashMap<>();
  protected SortedSet<K> keySet = new TreeSet<>(
      Comparator.comparing((K k) -> attributeMap.getOrDefault(k, null)).thenComparing(k -> k));

  @Override
  public K evict() {
    var r = keySet.first();
    keySet.remove(r);
    attributeMap.remove(r);
    return r;
  }

  @Override
  public void delete(K key) {
    keySet.remove(key);
    attributeMap.remove(key);
  }
}
