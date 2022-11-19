package com.github.amshali.asynchronous.sumofprime;

public interface ResponseStore {
  /**
   * Add or update a response in the response store.
   */
  void updateResponse(SumPrimeResponse response);

  /**
   * Finds and return the response associated with the requestId.
   * Returns null of no such response exists.
   */
  SumPrimeResponse getResponse(String requestId);
}
