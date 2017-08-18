package com.mikerussell.tempo.api;

public class Issue {
  private String key;
  private int remainingEstimateSeconds;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Integer getRemainingEstimateSeconds() {
    return remainingEstimateSeconds;
  }

  public void setRemainingEstimateSeconds(Integer remainingEstimateSeconds) {
    this.remainingEstimateSeconds = remainingEstimateSeconds;
  }
}
