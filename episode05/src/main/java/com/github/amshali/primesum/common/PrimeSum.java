package com.github.amshali.primesum.common;

import java.util.ArrayList;
import java.util.List;

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

  public static Long sumPrime(int a, int b) {
    var sum = 0L;
    for (int i = a; i < b; i++) {
      sum += (isPrime(i) ? i : 0);
    }
    return sum;
  }

  public static List<SumPrimeRequest> generateSplits(SumPrimeRequest request, int splitSize) {
    var l = new ArrayList<SumPrimeRequest>();
    var prevEnd = request.a;
    while (prevEnd < request.b) {
      l.add(new SumPrimeRequest(prevEnd, Math.min(request.b, prevEnd + splitSize)));
      prevEnd += splitSize;
    }
    return l;
  }
}
