package com.github.amshali.basiccache;

public interface EvictionManager<K extends Comparable<K>> {
  /**
   * Accesses a key. Can be called when a key is get or set.
   *
   * @param key Cache key
   */
  void access(K key);

  /**
   * Selects and evicts a key.
   *
   * @return the evicted key.
   */
  K evict();

  /**
   * Deletes a key.
   * @param key
   */
  void delete(K key);
}
