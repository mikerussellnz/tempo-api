# tempo-api
Atlassian Tempo API for Java and Example Application

## creating a worklog

    TempoApiClient tempoApiClient = new TempoApiClient(tempoUrl, username, password);
    Worklog worklog = new Worklog();
    Issue issue = new Issue();
    issue.setKey("JIRA-691");
    worklog.setIssue(issue);
    worklog.setAuthor(new Author(username));
    worklog.setComment(appointment.getSubject());
    worklog.setTimeSpentSeconds((int) appointment.getDuration().getTotalSeconds());
    worklog.setDateStarted(TempoApiClient.formatDateTime(new Date()));
    tempoApiClient.createWorkLog(worklog);

## retrieving worklogs
    TempoApiClient tempoApiClient = new TempoApiClient(tempoUrl, username, password);
    Worklog[] worklogs = tempoApiClient.getWorklogs(startDate, endDate);
