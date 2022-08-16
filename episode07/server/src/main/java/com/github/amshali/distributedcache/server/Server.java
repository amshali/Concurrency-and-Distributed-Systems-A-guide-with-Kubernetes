package com.github.amshali.distributedcache.server;

import com.github.amshali.zk.ZooKeeperConnection;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Server implements ApplicationRunner {

  public static Double TWO_PI = 2 * Math.PI;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final TreeSet<String> workersSortedSet = new TreeSet<>(
      Comparator.comparing(Server::ringHash));
  private ZooKeeperConnection zooKeeper;
  private String fZooKeeperWorkersPath;
  private Integer fWorkerRingReplicas = 50;
  private final WorkerStub workerStub = new WorkerStub();

  public static void main(String[] args) {
    SpringApplication.run(Server.class, args);
  }

  /**
   * Returns a number between 0(inclusive) and 360(exclusive) which represent the location
   * of the key on the hash ring.
   *
   * @param key key to hash.
   * @return a number between 0(inclusive) and 360(exclusive)
   */
  public static Double ringHash(String key) {
    var m = DigestUtils.sha1Hex(key).hashCode() % TWO_PI;
    if (m < 0) {
      m += TWO_PI;
    }
    return m;
  }

  private String findKeyLocation(String key) {
    lock.readLock().lock();
    try {
      var worker = workersSortedSet.ceiling(key);
      if (worker == null) {
        worker = workersSortedSet.first();
      }
      // Remove the '-<N>' from the end of location to extract the worker's address
      var dashIndex = worker.lastIndexOf("-");
      return worker.substring(0, dashIndex);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Scheduled(initialDelay = 1000, fixedRate = 3000)
  private void fetchWorkersList() throws InterruptedException, KeeperException {
    lock.writeLock().lock();
    try {
      var workers = zooKeeper.list(fZooKeeperWorkersPath);
      workersSortedSet.clear();
      if (workers != null) {
        // To keep it consistent between runs, we sort the received list.
        workers.sort(String::compareTo);
        workers.forEach(w -> IntStream.range(0, fWorkerRingReplicas)
            .forEach(i -> workersSortedSet.add(w + "-" + i)));
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  @PutMapping("/set/{key}")
  @ResponseBody
  public Future<String> set(@PathVariable String key, @RequestBody String value) throws URISyntaxException {
    var workerAddress = findKeyLocation(key);
    var response = workerStub.set(workerAddress, key, value);
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new ResponseStatusException(response.getStatusCode());
    }
    return new AsyncResult<>(response.getBody());
  }

  @GetMapping("/get/{key}")
  @ResponseBody
  public AsyncResult<String> get(@PathVariable String key) throws URISyntaxException {
    var workerAddress = findKeyLocation(key);
    var response = workerStub.get(workerAddress, key);
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new ResponseStatusException(response.getStatusCode());
    }
    return new AsyncResult<>(response.getBody());
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    String fZooKeeperHost = args.getOptionValues("zookeeper").get(0);
    fZooKeeperWorkersPath = args.getOptionValues("zk_workers_path").get(0);
    fWorkerRingReplicas = Integer.parseInt(args.getOptionValues("worker_ring_replicas").get(0));
    zooKeeper = new ZooKeeperConnection(fZooKeeperHost, () -> {
    });
  }
}
