package com.mikerussell.tempo.api;

import org.apache.http.auth.AUTH;

import java.util.ArrayList;
import java.util.List;

public class Worklog {
  private Author author;
  private Issue issue;
  private String dateStarted;
  private int timeSpentSeconds;
  private String comment;
  private List<KeyValuePair> worklogAttributes = new ArrayList<KeyValuePair>();

  public Author getAuthor() {
    return this.author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

  public Issue getIssue() {
    return issue;
  }

  public void setIssue(Issue issue) {
    this.issue = issue;
  }

  public String getDateStarted() {
    return dateStarted;
  }

  public void setDateStarted(String dateStarted) {
    this.dateStarted = dateStarted;
  }

  public int getTimeSpentSeconds() {
    return timeSpentSeconds;
  }

  public void setTimeSpentSeconds(int timeSpentSeconds) {
    this.timeSpentSeconds = timeSpentSeconds;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public List<KeyValuePair> getWorklogAttributes() {
    return worklogAttributes;
  }

  public void setWorklogAttributes(List<KeyValuePair> workLogAttributes) {
    this.worklogAttributes = workLogAttributes;
  }
}
