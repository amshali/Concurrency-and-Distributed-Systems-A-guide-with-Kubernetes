package com.github.amshali.distributedcache.worker;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperConnection {
  public static final int ZK_SESSION_TIMEOUT = 5000;
  public static final String SLASH = "/";
  private final ZooKeeper zooKeeper;
  final CountDownLatch connectedSignal = new CountDownLatch(1);

  public ZooKeeperConnection(String host) throws IOException,InterruptedException {

    zooKeeper = new ZooKeeper(host,ZK_SESSION_TIMEOUT, we -> {
      if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
        connectedSignal.countDown();
      }
    });

    connectedSignal.await();
  }

  public void createIgnoreExists(String path, CreateMode createMode) throws KeeperException, InterruptedException {
    try {
      zooKeeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
    } catch (KeeperException e) {
      if (e.code() != KeeperException.Code.NODEEXISTS) {
        throw e;
      }
    }
  }

  public void createWithParents(String path, CreateMode createMode) throws InterruptedException,
      KeeperException {
    String[] subPaths = path.split("/");
    StringBuilder sb = new StringBuilder();
    for (var p : subPaths) {
      if (p.isEmpty()) {
        continue;
      }
      sb.append(SLASH).append(p);
      createIgnoreExists(sb.toString(), createMode);
    }
  }
}
