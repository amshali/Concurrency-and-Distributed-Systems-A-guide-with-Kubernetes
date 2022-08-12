package com.github.amshali.distributedcache.worker;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
  public void run(ApplicationArguments args) throws IOException, InterruptedException, KeeperException {
    String fZooKeeperHost = args.getOptionValues("zookeeper").get(0);
    zooKeeper = new ZooKeeperConnection(fZooKeeperHost);
    String fZooKeeperWorkersPath = args.getOptionValues("zk_workers_path").get(0);
    zooKeeper.createWithParents(fZooKeeperWorkersPath, CreateMode.PERSISTENT);
    var myPodIp = System.getenv("MY_POD_IP");
    zooKeeper.createIgnoreExists(fZooKeeperWorkersPath + "/" + myPodIp, CreateMode.EPHEMERAL);
  }
}
