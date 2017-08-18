package com.mikerussell.tempo.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.RequestBodyEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TempoApiClient {
  private String baseURL;
  private String username;
  private String password;

  public TempoApiClient(String baseURL, String username, String password) {
    this.baseURL = baseURL;
    this.username = username;
    this.password = password;

    Unirest.setObjectMapper(new JacksonObjectMapper());
  }

  public static String formatDateTime(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000");
    String dateStr = dateFormat.format(date);
    return dateStr;
  }

  public static Date parseDateTime(String dateString) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    Date date = dateFormat.parse(dateString);
    return date;
  }

  public static String formatDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = dateFormat.format(date);
    return dateStr;
  }

  private String constructURL(String path) {
    return baseURL.endsWith("/") ? baseURL + path : baseURL + "/" + path;
  }

  public Worklog[] getWorklogs(Date from, Date to) throws UnirestException {
    GetRequest request = Unirest.get(constructURL("tempo-timesheets/3/worklogs"))
      .basicAuth(username, password).header("accept", "application/json");
    if (from != null) {
      request.queryString("dateFrom", formatDate(from));
    }
    if (to != null) {
      request.queryString("dateTo", formatDate(to));
    }

    HttpResponse<Worklog[]> response = request.asObject(Worklog[].class);
    return response.getBody();
  }

  public String createWorkLog(Worklog worklog) throws UnirestException {
    RequestBodyEntity request = Unirest.post(constructURL("tempo-timesheets/3/worklogs"))
      .basicAuth(username, password)
      .header("accept", "application/json")
      .header("Content-Type", "application/json")
      .body(worklog);

    HttpResponse<String> response = request.asString();
    if (response.getStatus() != 200) {
      throw new UnirestException("Error posting worklog: " + response.getBody());
    }
    return response.getBody();
  }
}
