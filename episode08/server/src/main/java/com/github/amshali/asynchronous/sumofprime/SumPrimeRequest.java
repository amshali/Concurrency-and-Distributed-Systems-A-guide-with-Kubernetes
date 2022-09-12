package com.github.amshali.asynchronous.sumofprime;

public class SumPrimeRequest {
  public int a;
  public int b;

  private String requestId;

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

  public String requestId() {
    return requestId;
  }

  public SumPrimeRequest setRequestId(String requestId) {
    this.requestId = requestId;
    return this;
  }
}
