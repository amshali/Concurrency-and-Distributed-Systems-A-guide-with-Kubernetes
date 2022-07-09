package com.github.amshali.primesum.common;

public class SumPrimeRequest {
  public int a;
  public int b;

  public SumPrimeRequest() {
  }

  public SumPrimeRequest(int a, int b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public String toString() {
    return "SumPrimeRequest{" +
        "a=" + a +
        ", b=" + b +
        '}';
  }
}
