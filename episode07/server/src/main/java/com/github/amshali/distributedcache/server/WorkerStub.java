package com.github.amshali.distributedcache.server;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class WorkerStub {
  private final RestTemplate rest;

  public WorkerStub() {
    rest = new RestTemplate();
  }

  public String set(String workerAddress, String key, String value) {
    var uri = URI.create(workerAddress).toString();
    return rest.exchange(uri + "/set/" + key,
        HttpMethod.POST, new HttpEntity<>(value, null),
        String.class).getBody();
  }

  public String get(String workerAddress, String key) {
    var uri = URI.create(workerAddress).toString();
    return rest.exchange(uri + "/get/" + key,
        HttpMethod.GET, new HttpEntity<>(null),
        String.class).getBody();
  }
}
