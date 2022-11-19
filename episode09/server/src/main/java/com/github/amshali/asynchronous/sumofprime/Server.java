package com.github.amshali.asynchronous.sumofprime;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Server implements ApplicationRunner {
  public static final int TTL_MS = 600_000;
  public static final int MAX_INPUT = 100_000_000;
  private Semaphore concurrencySemaphore;
  private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
  private ExecutorService executorService;
  /**
   * Maximum number of request that can be run on this server. Set by the flag `max_concurrency`.
   */
  private int maxConcurrency = 1;
  private ResponseStore responseStore;
  private String queueUrl;
  private int requestTimeoutSec;

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
        .withStatus(SumPrimeResponse.Status.RECEIVED);
    response.queuedTimeMs = now;
    responseStore.updateResponse(response);

    // Enqueue the request:
    Gson gson = new Gson();
    gson.toJson(request);
    SendMessageRequest sendSqsRequest = new SendMessageRequest()
        .withQueueUrl(queueUrl)
        .withMessageBody(gson.toJson(request))
        .withDelaySeconds(1);
    sqs.sendMessage(sendSqsRequest);
    response.setStatus(SumPrimeResponse.Status.QUEUED);
    responseStore.updateResponse(response);

    return response;
  }

  @GetMapping("/sumPrime/{requestId}")
  public SumPrimeResponse get(@PathVariable String requestId) {
    var r = responseStore.getResponse(requestId);
    if (r == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return r;
  }

  private Callable<Void> runRequest(SumPrimeRequest request, String messageHandle) {
    return () -> {
      System.out.println(request);
      var stopWatch = new StopWatch();
      stopWatch.start();
      var localResponse = responseStore.getResponse(request.requestId());
      // Set the status of the response to IN_PROGRESS:
      localResponse.status = SumPrimeResponse.Status.IN_PROGRESS;
      responseStore.updateResponse(localResponse);
      localResponse = responseStore.getResponse(request.requestId());
      // Compute:
      localResponse.sum = PrimeSum.sumPrime(request.a, request.b);
      // Set the status to FINISHED:
      localResponse.status = SumPrimeResponse.Status.FINISHED;
      localResponse.ttl = System.currentTimeMillis() + TTL_MS;
      stopWatch.stop();
      localResponse.processingTimeMs = stopWatch.getTotalTimeMillis();
      responseStore.updateResponse(localResponse);
      sqs.deleteMessage(queueUrl, messageHandle);
      System.out.println("Done: " + request);
      return null;
    };
  }

  @Scheduled(fixedDelay = 10, initialDelay = 2000)
  private void retrieveRequestFromSqs() throws InterruptedException {
    concurrencySemaphore.acquire();
    var receiveMessageResult =
        sqs.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl)
            .withMaxNumberOfMessages(1).withVisibilityTimeout(requestTimeoutSec)
            .withWaitTimeSeconds(20));
    if (receiveMessageResult.getMessages().isEmpty()) {
      concurrencySemaphore.release();
    } else {
      var msg = receiveMessageResult.getMessages().get(0);
      System.out.println("Got message: " + msg.getBody());
      Gson gson = new Gson();
      // create task...
      var future = executorService.submit(runRequest(gson.fromJson(msg.getBody(),
              SumPrimeRequest.class), msg.getReceiptHandle()));
      // Monitor the task and cancel if it exceeds the response timeout.
      executorService.submit(() -> {
        try {
          future.get(requestTimeoutSec - 1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          future.cancel(true);
        }
        concurrencySemaphore.release();
      });
    }
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    var fResponseStoreClass = args.getOptionValues("response_store_class").get(0);
    queueUrl = args.getOptionValues("queue_url").get(0);
    maxConcurrency = Integer.parseInt(args.getOptionValues("max_concurrency").get(0));
    requestTimeoutSec = Integer.parseInt(args.getOptionValues("request_timeout_sec").get(0));
    var clazz = Class.forName(fResponseStoreClass);
    responseStore = (ResponseStore) clazz.getConstructor().newInstance();
    concurrencySemaphore = new Semaphore(maxConcurrency, true);
    executorService = Executors.newFixedThreadPool(maxConcurrency * 2);
  }
}