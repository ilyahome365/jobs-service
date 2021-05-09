package com.home365.jobservice.service;

import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.mail.MailDetails;

import java.util.List;

public interface MailService {
    void sendMail(MailDetails mailDetails);
    void sendMailFromJobExecuteResults(JobExecutionResults jobExecutionResults, String from, List<String> recipientList, String templateName);

}
