package com.github.amshali.asynchronous.sumofprime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Server implements ApplicationRunner {
  public static final int MAX_INPUT = 100_000_000;
  public static final int TTL_MS = 600_000;
  private ExecutorService executorService;
  private ResponseStore responseStore;

  public static void main(String[] args) {
    SpringApplication.run(Server.class, args);
  }

  @PostMapping("/sumPrime")
  public SumPrimeResponse sumPrime(
      @RequestBody SumPrimeRequest request) {
    if (request.a < 0 || request.b < 0 || request.a >= request.b
        || request.a > MAX_INPUT || request.b > MAX_INPUT) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inputs.");
    }
    var requestId = UUID.randomUUID().toString();
    request.setRequestId(requestId);
    final var now = System.currentTimeMillis();
    // Create a response with status QUEUED and add it to the response store:
    var response = new SumPrimeResponse(requestId)
        .withStatus(SumPrimeResponse.Status.QUEUED);
    responseStore.updateResponse(response);
    // Submit a task to the executor's queue:
    executorService.submit(createTask(requestId, request, now));
    return response;
  }

  private Callable<Void> createTask(String requestId,
                                    SumPrimeRequest request, Long queuedTime) {
    return () -> {
      var stopWatch = new StopWatch();
      stopWatch.start();
      var localResponse = responseStore.getResponse(requestId);
      // Set the status of the response to IN_PROGRESS:
      localResponse.status = SumPrimeResponse.Status.IN_PROGRESS;
      localResponse.queuedTimeMs = System.currentTimeMillis() - queuedTime;
      responseStore.updateResponse(localResponse);
      localResponse = responseStore.getResponse(requestId);
      // Compute:
      localResponse.sum = PrimeSum.sumPrime(request.a, request.b);
      // Set the status to FINISHED:
      localResponse.status = SumPrimeResponse.Status.FINISHED;
      localResponse.ttl = System.currentTimeMillis() + TTL_MS;
      stopWatch.stop();
      localResponse.processingTimeMs = stopWatch.getTotalTimeMillis();
      responseStore.updateResponse(localResponse);
      return null;
    };
  }

  @GetMapping("/sumPrime/{requestId}")
  public SumPrimeResponse get(@PathVariable String requestId) {
    var r = responseStore.getResponse(requestId);
    if (r == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return r;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    var fResponseStoreClass = args.getOptionValues("response_store_class").get(0);
    var clazz = Class.forName(fResponseStoreClass);
    responseStore = (ResponseStore) clazz.getConstructor().newInstance();
    executorService = Executors.newFixedThreadPool(5);
  }
}