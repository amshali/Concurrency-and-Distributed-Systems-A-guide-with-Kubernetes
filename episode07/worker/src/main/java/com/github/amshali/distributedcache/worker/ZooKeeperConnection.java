package com.github.amshali.distributedcache.worker;

import org.apache.zookeeper.*;

import java.io.IOException;

public class ZooKeeperConnection {
  public static final int ZK_SESSION_TIMEOUT = 5000;
  public static final String SLASH = "/";
  private ZooKeeper zooKeeper = null;
  private Runnable onConnect = null;
  private String zooKeeperHost = null;
  final private Watcher watcher = we -> {
    if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
      System.out.println(">>>>> Connected");
      onConnect.run();
    }
    if (we.getState() == Watcher.Event.KeeperState.Expired) {
      System.out.println(">>>>> Expired");
      try {
        zooKeeper = new ZooKeeper(zooKeeperHost, ZK_SESSION_TIMEOUT, this.watcher);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  };

  public ZooKeeperConnection(String host, Runnable onConnect) throws IOException {
    this.zooKeeperHost = host;
    this.onConnect = onConnect;
    zooKeeper = new ZooKeeper(this.zooKeeperHost, ZK_SESSION_TIMEOUT, watcher);
  }

  public void createIgnoreExists(String path, CreateMode createMode) throws KeeperException,
      InterruptedException {
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
