package com.mikerussell.tempo.tool;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mikerussell.tempo.api.Author;
import com.mikerussell.tempo.api.Issue;
import com.mikerussell.tempo.api.TempoApiClient;
import com.mikerussell.tempo.api.Worklog;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import org.apache.commons.cli.*;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class App {

  public static Worklog createWorkLog(String username) {
    Worklog newWorklog = new Worklog();
    Issue issue = new Issue();
    issue.setKey("GDCPLF-691");
    newWorklog.setAuthor(new Author(username));
    newWorklog.setIssue(issue);
    return newWorklog;
  }

  private static Date truncateToDate(Date startTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startTime);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  public static void main(String [] arguments) throws Exception {
    String tempoUrl = "https://jira.company.com/jira/rest";
    String exchangeSvcAutoDiscoverUrl = "https://autodiscover.company.com/EWS/Exchange.asmx";

    String username = null;
    String password = null;
    String startDateStr = null;
    String endDateStr = null;

    Options options = new Options();
    options.addRequiredOption("u","username", true, "username to use (e-mail address)");
    options.addRequiredOption("p","password", true, "password to use (your password)");
    options.addRequiredOption("s","start", true, "start date (inclusive, format = 2017-07-20)");
    options.addRequiredOption("e","end", true, "end date (inclusive), format = 2017-07-27");
    options.addOption("h","help", false, "displays this help screen");

    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, arguments);
    } catch (MissingOptionException e) {
      System.err.println("Error: " + e.getMessage());
      displayHelp(options);
      System.exit(1);
    }
    if (line.hasOption("u")) {
      username = line.getOptionValue("u");
    }
    if (line.hasOption("p")) {
      password = line.getOptionValue("p");
    }
    if (line.hasOption("s")) {
      startDateStr = line.getOptionValue("s");
    }
    if (line.hasOption("e")) {
      endDateStr = line.getOptionValue("e");
    }
    if (line.hasOption("h")) {
      displayHelp(options);
      System.exit(0);
    }

    System.out.println("Synchronizing " + username + " for dates " + startDateStr + " until " + endDateStr);

    TempoApiClient tempoApiClient = new TempoApiClient(tempoUrl, username, password);

    ExchangeService exchangeApiClient = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
    ExchangeCredentials credentials = new WebCredentials(username, password);
    exchangeApiClient.setCredentials(credentials);
    exchangeApiClient.setUrl(new URI(exchangeSvcAutoDiscoverUrl));

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date startDate = formatter.parse(startDateStr + " 12:00:00");
    Date endDate = formatter.parse(endDateStr + " 23:59:59");

    System.out.println("Loading worklogs from tempo...");
    Map<Date, List<Worklog>> worklogs = findWorklogs(tempoApiClient, startDate, endDate);
    System.out.println("Loading appointments from exchange...");
    List<Appointment> appointments = findAppointments(exchangeApiClient, startDate, endDate);

    System.out.println("Finding missing worklogs...");
    List<Appointment> missingWorklogs = findMissingAppointments(worklogs, appointments);

    System.out.println("Found " + missingWorklogs.size() + " missing worklogs.");

    if (missingWorklogs.size() == 0) {
      System.exit(0);
    }

    System.out.println("Creating worklogs for missing entries...");
    for (Appointment appointment : missingWorklogs) {
      System.out.println("Creating worklog for:");
      System.out.println("START: " + appointment.getStart());
      System.out.println("DURATION: " + appointment.getDuration());
      System.out.println("SUBJECT: " + appointment.getSubject());

      System.out.println("Posting to tempo....");
      Worklog worklog = createWorkLog(username);
      worklog.setComment(appointment.getSubject());
      worklog.setTimeSpentSeconds((int) appointment.getDuration().getTotalSeconds());
      worklog.setDateStarted(TempoApiClient.formatDateTime(appointment.getStart()));

      tempoApiClient.createWorkLog(worklog);
      System.out.println("Posted to tempo successfully.");
      System.out.println("====================================");
    }
  }

  private static void displayHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("tempotool", options);
  }

  private static List<Appointment> findMissingAppointments(Map<Date, List<Worklog>> worklogs,
    List<Appointment> appointments) throws ServiceLocalException {

    List<Appointment> missingWorklogs = new ArrayList<Appointment>();
    for (Appointment appt : appointments) {
      // ignore cancelled appts.
      if (appt.getIsCancelled()) {
        continue;
      }
      Date startTime = appt.getStart();
      Date date = truncateToDate(startTime);

      List<Worklog> worklogsForDate = worklogs.get(date);
      boolean found = false;
      if (worklogsForDate != null) {
        for (Worklog worklog : worklogsForDate) {
          if (worklog.getComment() != null) {
            if (worklog.getComment().equals(appt.getSubject())) {
              found = true;
              break;
            }
          }
        }
      }

      if (!found) {
        missingWorklogs.add(appt);
      }
    }
    return missingWorklogs;
  }

  private static Map<Date, List<Worklog>> findWorklogs(TempoApiClient tempoApiClient, Date startDate, Date endDate)
    throws UnirestException, ParseException {
    Worklog[] worklogs = tempoApiClient.getWorklogs(startDate, endDate);

    Map<Date, List<Worklog>> worklogsmap = new HashMap<Date, List<Worklog>>();
    for (Worklog worklog : worklogs) {
      Date worklogTime = TempoApiClient.parseDateTime(worklog.getDateStarted());

      List<Worklog> worklogsForTime = worklogsmap.get(worklogTime);
      if (worklogsForTime == null) {
        worklogsForTime = new ArrayList<Worklog>();
        worklogsmap.put(worklogTime, worklogsForTime);
      }
      worklogsForTime.add(worklog);
    }
    return worklogsmap;
  }

  public static List<Appointment> findAppointments(ExchangeService service, Date startDate, Date endDate) throws Exception {
    CalendarFolder calendarFolder = CalendarFolder.bind(service, WellKnownFolderName.Calendar);
    FindItemsResults<Appointment> findResults = calendarFolder.findAppointments(new CalendarView(startDate, endDate));
    ArrayList<Appointment> appointments = findResults.getItems();

    for (Appointment appointment : appointments) {
      appointment.load(PropertySet.FirstClassProperties);
    }
    return appointments;
  }
}
