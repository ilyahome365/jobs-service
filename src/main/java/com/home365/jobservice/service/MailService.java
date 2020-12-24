package com.home365.jobservice.service;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;

import java.util.List;

public interface MailService {
    MailResult sendMail(MailDetails mailDetails);






    MailResult sendMailFromJobExecuteResults(JobExecutionResults jobExecutionResults, String from, List<String> recipientList, String TemplateName);
}
