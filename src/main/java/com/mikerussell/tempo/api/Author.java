package com.mikerussell.tempo.api;

public class Author {
  private String name;

  public Author() {}

  public Author(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
