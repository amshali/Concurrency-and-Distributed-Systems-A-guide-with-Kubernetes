package com.github.amshali.asynchronous.sumofprime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SumPrimeResponse {
  public String requestId;
  public Status status;
  public Long sum;
  public Long processingTimeMs;
  public Long queuedTimeMs;

  public Long ttl;
  private String rev;

  public SumPrimeResponse() {
  }

  public SumPrimeResponse(String requestId) {
    this.requestId = requestId;
  }

  public Long ttl() {
    return ttl;
  }

  public void setTtl(Long ttl) {
    this.ttl = ttl;
  }

  @JsonProperty("_rev")
  public String rev() {
    return rev;
  }

  @JsonProperty("_rev")
  public void setRev(String rev) {
    this.rev = rev;
  }

  @JsonProperty("_id")
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Status status() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Long sum() {
    return sum;
  }

  public void setSum(Long sum) {
    this.sum = sum;
  }

  public Long processingTimeMs() {
    return processingTimeMs;
  }

  public void setProcessingTimeMs(Long processingTimeMs) {
    this.processingTimeMs = processingTimeMs;
  }

  public Long queuedTimeMs() {
    return queuedTimeMs;
  }

  public void setQueuedTimeMs(Long queuedTimeMs) {
    this.queuedTimeMs = queuedTimeMs;
  }

  @JsonProperty("_id")
  public String requestId() {
    return requestId;
  }

  public SumPrimeResponse withStatus(Status status) {
    this.status = status;
    return this;
  }

  @Override
  public String toString() {
    return "SumPrimeResponse{" +
        "requestId='" + requestId + '\'' +
        ", status=" + status +
        ", sum=" + sum +
        ", processingTimeMs=" + processingTimeMs +
        ", queuedTimeMs=" + queuedTimeMs +
        ", rev='" + rev + '\'' +
        '}';
  }

  public enum Status {
    RECEIVED,
    QUEUED,
    IN_PROGRESS,
    FINISHED
  }
}
