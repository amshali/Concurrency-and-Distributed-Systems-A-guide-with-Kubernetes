package com.github.amshali.primesum.worker;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class ServerStub {
  private final String serverAddress;
  private final RestTemplate rest;

  public ServerStub(String serverAddress) {
    this.serverAddress = URI.create(serverAddress).toString();
    rest = new RestTemplate();
  }

  public String register(String workerId) {
    try {
      return rest.exchange(serverAddress + "/worker/register/{workerId}",
          HttpMethod.PUT, new HttpEntity<>(null, null), String.class, workerId).getBody();
    } catch (RestClientException e) {
      System.err.println(e);
    }
    return null;
  }

}
