package com.github.amshali.distributedcache.worker;

import com.github.amshali.basiccache.CacheImpl;
import com.github.amshali.basiccache.LfuEvictionManager;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.concurrent.Future;

@SpringBootApplication
@RestController
@EnableScheduling
@Service
@EnableAutoConfiguration
public class Worker implements ApplicationRunner {

  private ZooKeeperConnection zooKeeper;

  public static void main(String[] args) {
    SpringApplication.run(Worker.class, args);
  }

  @GetMapping("/actuator/healthz")
  public String healthz() {
    return "healthy";
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    String fZooKeeperHost = args.getOptionValues("zookeeper").get(0);
    String fZooKeeperWorkersPath = args.getOptionValues("zk_workers_path").get(0);
    zooKeeper = new ZooKeeperConnection(fZooKeeperHost, () -> {
      try {
        zooKeeper.createWithParents(fZooKeeperWorkersPath, CreateMode.PERSISTENT);
        var myPodIp = System.getenv("MY_POD_IP");
        zooKeeper.createIgnoreExists(fZooKeeperWorkersPath + "/" + myPodIp, CreateMode.EPHEMERAL);
      } catch (InterruptedException | KeeperException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private CacheImpl<String, String> cacheShard = new CacheImpl<>(new LfuEvictionManager<String>(),
      800_000, 20);

  @PutMapping("/{key}")
  public Future<String> set(@PathVariable String key, @RequestBody String data) {
    cacheShard.set(key, data);
    return new AsyncResult<>(key);
  }

  @GetMapping("/{key}")
  public AsyncResult<String> get(@PathVariable String key) {
    var v = cacheShard.get(key);
    if (v == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return new AsyncResult<>(v);
  }

}
