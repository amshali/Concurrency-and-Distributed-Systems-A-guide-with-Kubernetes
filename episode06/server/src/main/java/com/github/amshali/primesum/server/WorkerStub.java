package com.github.amshali.primesum.server;

import com.github.amshali.primesum.common.SumPrimeRequest;
import com.github.amshali.primesum.common.SumPrimeResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

public class WorkerStub {
  public String workerId() {
    return workerId;
  }

  private final String workerId;
  private final String workerAddress;
  private final RestTemplate rest;

  public WorkerStub(String workerId, String workerAddress) {
    this.workerId = workerId;
    this.workerAddress = URI.create(workerAddress).toString();
    rest = new RestTemplate();
  }

  public SumPrimeResponse sumPrime(SumPrimeRequest request) {
    try {
      return rest.exchange(workerAddress + "/sumPrime",
          HttpMethod.POST, new HttpEntity<>(request, null),
          SumPrimeResponse.class).getBody();
    } catch (RestClientException e) {
      System.err.println(e);
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WorkerStub that = (WorkerStub) o;
    return Objects.equals(workerAddress, that.workerAddress);
  }
}
