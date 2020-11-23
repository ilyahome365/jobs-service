package com.home365.jobservice.service;

import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.mail.MailDetails;

public interface MailService {
    JobExecutionResults sendMail(MailDetails mailDetails);
}
