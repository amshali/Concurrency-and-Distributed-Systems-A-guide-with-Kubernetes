package com.github.amshali.basiccache;

public interface Cache<K, V> {
  V get(K key);

  void set(K key, V value);

  /**
   * Deletes a key from the cache.
   * @param key
   */
  void delete(K key);

  int size();
}
