package com.github.amshali.distributedcache.server;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WorkerStub {
  private final RestTemplate rest;

  public WorkerStub() {
    rest = new RestTemplate();
    rest.setErrorHandler(new ResponseErrorHandler() {
      @Override
      public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
      }

      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
      }
    });
  }

  public ResponseEntity<String> set(String workerAddress, String key, String value) throws URISyntaxException {
    var uri = new URI("http", workerAddress, "/set/" + key, null).toString();
    return rest.exchange(uri,
        HttpMethod.PUT, new HttpEntity<>(value, null),
        String.class);
  }

  public ResponseEntity<String> get(String workerAddress, String key) throws URISyntaxException {
    var uri = new URI("http", workerAddress, "/get/" + key, null).toString();
    return rest.exchange(uri,
        HttpMethod.GET, new HttpEntity<>(null),
        String.class);
  }
}
