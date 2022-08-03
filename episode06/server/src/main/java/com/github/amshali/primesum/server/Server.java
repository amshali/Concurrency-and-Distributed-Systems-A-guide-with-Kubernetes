package com.github.amshali.primesum.server;

import com.github.amshali.primesum.common.PrimeSum;
import com.github.amshali.primesum.common.SumPrimeRequest;
import com.github.amshali.primesum.common.SumPrimeResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@RestController
@EnableAsync
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Server {

  public static final int SPLIT_SIZE = 2_500_000;
  private final Map<String, Long> workerLastUpdate = new ConcurrentHashMap<>();
  private final BlockingDeque<WorkerStub> freeWorkers = new LinkedBlockingDeque<>();
  private final ExecutorService taskExecutor = Executors.newFixedThreadPool(16);
  AtomicInteger inFlightTasks = new AtomicInteger(0);

  public static void main(String[] args) {
    SpringApplication.run(Server.class, args);
  }

  @Scheduled(fixedRate = 1000)
  public void workersHouseKeeping() {
    var now = System.currentTimeMillis();
    var workers = workerLastUpdate.keySet().toArray(new String[]{});
    for (var w : workers) {
      if (now - workerLastUpdate.get(w) > 5000) {
        freeWorkers.removeIf((ws) -> ws.workerId().equals(w));
        workerLastUpdate.remove(w);
      }
    }
    System.out.println("In flight tasks: " + inFlightTasks.get());
  }

  @PreDestroy
  public void onExit() {
    System.out.println("Exiting...");
    try {
      Thread.sleep(3000);
      System.out.println("Bye!");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/actuator/healthz")
  public String healthz() {
    return "healthy";
  }

  private Callable<SumPrimeResponse> createTask(SumPrimeRequest request) {
    return () -> {
      var worker = freeWorkers.take();
      var response = worker.sumPrime(request);
      freeWorkers.offer(worker);
      return response;
    };
  }

  @PostMapping("/sumPrime")
  public AsyncResult<SumPrimeResponse> sumPrime(@RequestBody SumPrimeRequest request) throws InterruptedException {
    if (inFlightTasks.get() > 50) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
    var stopWatch = new StopWatch();
    inFlightTasks.incrementAndGet();
    stopWatch.start();
    var futures =
        taskExecutor.invokeAll(PrimeSum.generateSplits(request, SPLIT_SIZE).stream().map(this::createTask).toList());
    var total = new AtomicLong(0);
    futures.forEach((v) -> {
      try {
        total.addAndGet(v.get().sum);
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    });
    stopWatch.stop();
    inFlightTasks.decrementAndGet();
    return new AsyncResult<>(new SumPrimeResponse(total.get(), stopWatch.getTotalTimeMillis()));
  }

  @PutMapping("/worker/register/{workerId}")
  public Future<String> register(@PathVariable String workerId) {
    if (!workerLastUpdate.containsKey(workerId)) {
      workerLastUpdate.put(workerId, System.currentTimeMillis());
      freeWorkers.offer(new WorkerStub(workerId, String.format("http://%s.%s.pod.cluster.local",
          workerId, System.getenv("MY_POD_NAMESPACE"))));
    } else {
      workerLastUpdate.put(workerId, System.currentTimeMillis());
    }
    return new AsyncResult<>("Registered.");
  }

  @GetMapping("/worker/list")
  public AsyncResult<List<String>> listWorkers() {
    return new AsyncResult<>(workerLastUpdate.keySet().stream().toList());
  }
}
