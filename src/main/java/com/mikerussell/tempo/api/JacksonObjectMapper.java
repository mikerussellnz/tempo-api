package com.mikerussell.tempo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.ObjectMapper;

import java.io.IOException;

public class JacksonObjectMapper implements ObjectMapper {
  private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  public JacksonObjectMapper() {
    jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public <T> T readValue(String value, Class<T> valueType) {
    try {
      return jacksonObjectMapper.readValue(value, valueType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String writeValue(Object value) {
    try {
      String json = jacksonObjectMapper.writeValueAsString(value);
      return json;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
