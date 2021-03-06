package com.github.amshali.primesum.calculator;

import com.github.amshali.primesum.common.PrimeSum;
import com.github.amshali.primesum.common.SumPrimeRequest;
import com.github.amshali.primesum.common.SumPrimeResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@RestController
@Service
@EnableScheduling
@EnableAutoConfiguration
@EnableAsync
public class Calculator {

  /**
   * Counts the number of in flight queries for debugging purposes.
   */
  private AtomicInteger inFlight = new AtomicInteger(0);

  public static void main(String[] args) {
    System.out.println("Available cores: " + Runtime.getRuntime().availableProcessors());
    SpringApplication.run(Calculator.class, args);
  }

  @Scheduled(initialDelay = 2000, fixedRate = 1000)
  public void debug() {
    System.out.println("In flight queries: " + inFlight.get());
  }

  @PostMapping("/sumPrime")
  public AsyncResult<SumPrimeResponse> sumPrime(@RequestBody SumPrimeRequest request) {
    var stopWatch = new StopWatch();
    stopWatch.start();
    inFlight.incrementAndGet();
    Long sum = PrimeSum.sumPrime(request.a, request.b);
    inFlight.decrementAndGet();
    stopWatch.stop();
    return new AsyncResult<>(new SumPrimeResponse(sum, stopWatch.getTotalTimeMillis()));
  }
}
