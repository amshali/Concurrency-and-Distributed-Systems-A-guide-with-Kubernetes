package com.github.amshali.primesum.worker;

import com.github.amshali.primesum.common.PrimeSum;
import com.github.amshali.primesum.common.SumPrimeRequest;
import com.github.amshali.primesum.common.SumPrimeResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Worker implements ApplicationRunner {

  private Boolean shuttingDown = false;
  private ServerStub serverStub;

  public static void main(String[] args) {
    SpringApplication.run(Worker.class, args);
  }

  @Scheduled(initialDelay = 2000, fixedRate = 1000)
  public void registerWithServer() {
    if (shuttingDown || serverStub == null) {
      return;
    }
    var myPodIp = System.getenv("MY_POD_IP").replace('.', '-');
    serverStub.register(myPodIp);
  }

  @PreDestroy
  public void onExit() {
    shuttingDown = true;
    System.out.println("Exiting...");
    try {
      Thread.sleep(3000);
      System.out.println("Bye!");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/healthz")
  public ResponseEntity<String> healthz() {
    if (shuttingDown) {
      return ResponseEntity.status(500).body("shutting down");
    }
    return ResponseEntity.status(200).body("healthy");
  }

  @PostMapping("/sumPrime")
  public AsyncResult<SumPrimeResponse> sumPrime(@RequestBody SumPrimeRequest request) {
    var stopWatch = new StopWatch();
    stopWatch.start();
    var sum = PrimeSum.sumPrime(request.a, request.b);
    stopWatch.stop();
    return new AsyncResult<>(new SumPrimeResponse(sum, stopWatch.getTotalTimeMillis()));
  }

  @Override
  public void run(ApplicationArguments args) {
    String fCoordinator = args.getOptionValues("coordinator").get(0);
    serverStub = new ServerStub(fCoordinator);
  }
}
