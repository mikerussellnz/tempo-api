# tempo-api
Atlassian Tempo API for Java and Example Application

Allows connecting to Tempo to query and create work logs. 

## creating a worklog

    TempoApiClient tempoApiClient = new TempoApiClient(tempoUrl, username, password);
    Worklog worklog = new Worklog();
    Issue issue = new Issue();
    issue.setKey("JIRA-691");
    worklog.setIssue(issue);
    worklog.setAuthor(new Author(username));
    worklog.setComment("My Worklog Created with API);
    worklog.setTimeSpentSeconds(3600);
    worklog.setDateStarted(TempoApiClient.formatDateTime(new Date()));
    tempoApiClient.createWorkLog(worklog);

## retrieving worklogs
    TempoApiClient tempoApiClient = new TempoApiClient(tempoUrl, username, password);
    Worklog[] worklogs = tempoApiClient.getWorklogs(startDate, endDate);

## using the example app that synchronizes exchange calendar to worklogs

Set tempoUrl and exchangeSvcAutoDiscoverUrl appropriately in App.java.

Run App with appropriate command line arguments:

     --username user@company.com --password mysecretpassword --start 2017-08-14 --end 2017-08-18 
