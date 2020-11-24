package com.home365.jobservice.service;

import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;

public interface MailService {
    MailResult sendMail(MailDetails mailDetails);
}
