package com.github.amshali.distributedcache.server;

import com.github.amshali.zk.ZooKeeperConnection;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Server implements ApplicationRunner {

  private ZooKeeperConnection zooKeeper;
  private String fZooKeeperWorkersPath;
  private Integer fWorkerRingReplicas = 50;
  public static Double TWO_PI = 2 * Math.PI;
  public static void main(String[] args) {
//    SpringApplication.run(Server.class, args);
    var s = new Server();
    s.TEST();
  }

  /**
   * Returns a number between 0(inclusive) and 360(exclusive) which represent the location
   * of the key on the hash ring.
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

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final TreeSet<String> workersSortedSet = new TreeSet<>(
      Comparator.comparing(Server::ringHash));

  public void TEST() {
    System.out.println("key1".hashCode());
    System.out.println("key2".hashCode());
    for (int s = 1; s <= 3; s++) {
      for (int j = 0; j < fWorkerRingReplicas; j++) {
        workersSortedSet.add("server"+s + "-"+j);
      }
    }
    for (var w : workersSortedSet) {
      System.out.println(w + ": " + ringHash(w));
    }
    var countMap = new HashMap<String, Integer>();
    for (int i = 0; i < 100000; i++) {
      var k = "key" + Math.random();
      var l = findKeyLocation(k);
      countMap.putIfAbsent(l, 0);
      countMap.put(l, countMap.get(l) + 1);
    }
    System.out.println(countMap);
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

  @Override
  public void run(ApplicationArguments args) throws IOException {
    String fZooKeeperHost = args.getOptionValues("zookeeper").get(0);
    fZooKeeperWorkersPath = args.getOptionValues("zk_workers_path").get(0);
    fWorkerRingReplicas = Integer.parseInt(args.getOptionValues("worker_ring_replicas").get(0));
    zooKeeper = new ZooKeeperConnection(fZooKeeperHost, () -> {});
  }
}
