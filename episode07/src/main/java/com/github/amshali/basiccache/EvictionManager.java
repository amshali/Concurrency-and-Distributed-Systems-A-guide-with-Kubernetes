package com.github.amshali.basiccache;

public interface EvictionManager<K extends Comparable<K>> {
  /**
   * Accesses a key. Can be called when a key is get or set.
   *
   * @param key Cache key
   */
  void access(K key);

  /**
   * Selects a key to evict.
   *
   * @return the candidate key to evict.
   */
  K selectKeyToEvict();

  /**
   * Evicts a key.
   */
  void evict(K key);

  /**
   * Deletes a key.
   * @param key
   */
  void delete(K key);
}
