package com.github.amshali.asynchronous.sumofprime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PrimeSum {
  public static Boolean isPrime(int n) {
    if (n == 2 || n == 3) {
      return true;
    }
    if (n <= 1 || n % 2 == 0 || n % 3 == 0) {
      return false;
    }
    for (int i = 5; i * i <= n; i += 6) {
      if (n % i == 0 || n % (i + 2) == 0) {
        return false;
      }
    }
    return true;
  }

  public static Long sumPrime(int a, int b) throws InterruptedException {
    var sum = 0L;
    for (int i = a; i < b; i++) {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException();
      }
      sum += (isPrime(i) ? i : 0);
    }
    return sum;
  }
}
