package com.home365.jobservice.controller;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mails")
public class MailController {
    private final MailService mailService;
    private final AppProperties appProperties;

    public MailController(MailService mailService, AppProperties appProperties) {
        this.mailService = mailService;
        this.appProperties = appProperties;
    }

    @PostMapping("/send-mail-per-job")
    public ResponseEntity sendMail(@RequestBody JobExecutionResults jobExecutionResults) {
        log.info("POST / Send Mail for : {} ",jobExecutionResults);

        mailService.sendMailFromJobExecuteResults(jobExecutionResults, appProperties.getMailSupport(),
                appProperties.getJobExecutorMailToEmail(), appProperties.getJobExecuteTemplateName());
        return ResponseEntity.ok().build();
    }
}
