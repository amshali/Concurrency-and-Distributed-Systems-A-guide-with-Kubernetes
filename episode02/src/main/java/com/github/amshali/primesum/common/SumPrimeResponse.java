package com.github.amshali.primesum.common;

public class SumPrimeResponse {
  public Long sum;
  public Long processingTimeMs;

  public SumPrimeResponse() {
  }

  public SumPrimeResponse(Long sum, Long processingTimeMs) {
    this.sum = sum;
    this.processingTimeMs = processingTimeMs;
  }

  @Override
  public String toString() {
    return "SumPrimeResponse{" +
        "sum=" + sum +
        ", processingTimeMs=" + processingTimeMs +
        '}';
  }
}
