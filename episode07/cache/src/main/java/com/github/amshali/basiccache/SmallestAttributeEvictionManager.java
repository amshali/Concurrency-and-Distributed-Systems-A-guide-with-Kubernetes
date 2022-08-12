package com.github.amshali.basiccache;

import java.util.*;

public abstract class SmallestAttributeEvictionManager<K extends Comparable<K>,
    A extends Comparable<A>> implements EvictionManager<K> {

  protected final Map<K, A> attributeMap = new HashMap<>();
  /**
   * Must be initialized by the derived classes.
   */
  protected SortedSet<K> keySet = null;

  @Override
  public K selectKeyToEvict() {
    return keySet.first();
  }

  @Override
  public void evict(K key) {
    keySet.remove(key);
    attributeMap.remove(key);
  }

  @Override
  public void delete(K key) {
    keySet.remove(key);
    attributeMap.remove(key);
  }
}
