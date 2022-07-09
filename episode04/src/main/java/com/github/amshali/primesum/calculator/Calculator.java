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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
  private final int THROTTLING_THRESHOLD = Integer.valueOf(System.getenv("THROTTLING_THRESHOLD"));

  public static void main(String[] args) {
    System.out.println("Available cores: " + Runtime.getRuntime().availableProcessors());
    System.out.println("Throttling threshold at: " + System.getenv("THROTTLING_THRESHOLD"));
    SpringApplication.run(Calculator.class, args);
  }

  @Scheduled(initialDelay = 2000, fixedRate = 1000)
  public void debug() {
    System.out.println("In flight queries: " + inFlight.get());
  }

  @PostMapping("/sumPrime")
  public AsyncResult<SumPrimeResponse> sumPrime(@RequestBody SumPrimeRequest request) {
    if (inFlight.get() > THROTTLING_THRESHOLD) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
    inFlight.incrementAndGet();
    var stopWatch = new StopWatch();
    stopWatch.start();
    Long sum = PrimeSum.sumPrime(request.a, request.b);
    stopWatch.stop();
    inFlight.decrementAndGet();
    return new AsyncResult<>(new SumPrimeResponse(sum, stopWatch.getTotalTimeMillis()));
  }
}
