package com.home365.jobservice.executor;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class JobExecutorImpl implements JobService {
    protected final AppProperties appProperties;
    protected final MailService mailService;

    public JobExecutorImpl(AppProperties appProperties,
                           MailService mailService) {
        this.appProperties = appProperties;
        this.mailService = mailService;
    }

    @Override
    public JobExecutionResults executeJob() {
        JobExecutionResults jobExecutionResults = new JobExecutionResults();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            jobExecutionResults.setStartTime(startTime);
            String jobExecutionResult = execute();
            jobExecutionResults.setMessage(jobExecutionResult);
            setEndingTimeAndDuration(jobExecutionResults, startTime);
            sendMailOnFail(getJobName(), jobExecutionResults);
        } catch (Exception ex) {
            setEndingTimeAndDuration(jobExecutionResults, startTime);
            jobExecutionResults.setError(ex.getMessage());
            jobExecutionResults.setStackTrace(Arrays.toString(ex.getStackTrace()));
            log.info(String.format("Job [%s] failed -> Send Mail with the reason", getJobName()));
            sendMailOnFail(getJobName(), jobExecutionResults);
        }
        return jobExecutionResults;
    }

    protected abstract String getJobName();

    protected abstract String execute() throws Exception;

    private void setEndingTimeAndDuration(JobExecutionResults jobExecutionResults,
                                          LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        jobExecutionResults.setEndTime(endTime);
        jobExecutionResults.setElapsedTime(Duration.between(startTime, endTime));
    }

    private void sendMailOnFail(String subject, JobExecutionResults jobExecutionResults) {
        if (appProperties.getSendMailOnJobExecutorFail()) {
            MailDetails mailDetails = new MailDetails();
            mailDetails.setFrom(appProperties.getMailSupport());
            mailDetails.setSubject(subject);
            mailDetails.setTemplateName("job-executor-result");
            List<RecipientMail> recipientMails = ListUtils
                    .emptyIfNull(appProperties.getJobExecutorMailToEmail())
                    .stream()
                    .map(email -> {
                        RecipientMail recipientMail = new RecipientMail();
                        recipientMail.setName(appProperties.getJobExecutorMailToName());
                        recipientMail.setEmail(email);
                        return recipientMail;
                    }).collect(Collectors.toList());
            mailDetails.setRecipients(recipientMails);
            mailDetails.setContentTemplate(getContentTemplate(jobExecutionResults));

            MailResult mailResult = mailService.sendMail(mailDetails);
            log.info(mailResult.toString());
            return;
        }
        log.info("Job Executor failed -> Send Mail is disabled");
    }

    private Map<String, String> getContentTemplate(JobExecutionResults jobExecutionResults) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("START_TIME", jobExecutionResults.getStartTimeBeautify());
        contentTemplate.put("END_TIME", jobExecutionResults.getEndTimeBeautify());
        contentTemplate.put("ELAPSED_TIME", jobExecutionResults.getElapsedTime());
        contentTemplate.put("JOB_RESULT", StringUtils.isEmpty(jobExecutionResults.getMessage()) ? "" : jobExecutionResults.getMessage());
        contentTemplate.put("ERROR", StringUtils.isEmpty(jobExecutionResults.getError()) ? "" : jobExecutionResults.getError());
        contentTemplate.put("STACK_TRACE", StringUtils.isEmpty(jobExecutionResults.getStackTrace()) ? "" : jobExecutionResults.getStackTrace());
        return contentTemplate;
    }
}
