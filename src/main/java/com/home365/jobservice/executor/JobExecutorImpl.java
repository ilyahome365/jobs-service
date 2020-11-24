package com.home365.jobservice.executor;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JobExecutorImpl implements JobExecutor {
    private final AppProperties appProperties;
    private final MailService mailService;

    public JobExecutorImpl(AppProperties appProperties,
                           MailService mailService) {
        this.appProperties = appProperties;
        this.mailService = mailService;
    }

    @Override
    public JobExecutionResults executeJob(JobService jobService) {
        JobExecutionResults jobExecutionResults = new JobExecutionResults();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            jobExecutionResults.setStartTime(startTime);
            JobExecutionResult jobExecutionResult = jobService.executeJob();
            jobExecutionResults.setJobExecutionResult(jobExecutionResult);
            setEndingTimeAndDuration(jobExecutionResults, startTime);
            sendMailOnFail(jobExecutionResults);
        } catch (Exception ex) {
            setEndingTimeAndDuration(jobExecutionResults, startTime);
            jobExecutionResults.setError(ex.getMessage());
            jobExecutionResults.setStackTrace(Arrays.toString(ex.getStackTrace()));
            sendMailOnFail(jobExecutionResults);
        }
        return jobExecutionResults;
    }

    private void setEndingTimeAndDuration(JobExecutionResults jobExecutionResults,
                               LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        jobExecutionResults.setEndTime(endTime);
        jobExecutionResults.setElapsedTime(Duration.between(startTime, endTime));
    }

    private void sendMailOnFail(JobExecutionResults jobExecutionResults) {
        if (appProperties.getSendMailOnJobExecutorFail()) {
            log.info("Job Executor failed -> Send Mail with the reason");
            MailDetails mailDetails = new MailDetails();
            mailDetails.setFrom(appProperties.getMailSupport());
            mailDetails.setSubject("Job Executor failed");
            mailDetails.setTemplateName("job-executor-result");

            RecipientMail recipientMail = new RecipientMail();
            recipientMail.setName(appProperties.getJobExecutorMailToName());
            recipientMail.setEmail(appProperties.getJobExecutorMailToEmail());

            mailDetails.setRecipients(Collections.singletonList(recipientMail));
            mailDetails.setContentTemplate(getContentTemplate(jobExecutionResults));

            MailResult mailResult = mailService.sendMail(mailDetails);
            log.info(mailResult.toString());
            return;
        }
        log.info("Job Executor failed -> Send Mail is disabled");
    }

    private Map<String, String> getContentTemplate(JobExecutionResults jobExecutionResults) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("CONTENT", jobExecutionResults.toString());
        return contentTemplate;
    }
}
