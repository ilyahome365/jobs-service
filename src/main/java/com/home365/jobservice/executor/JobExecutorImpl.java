package com.home365.jobservice.executor;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public abstract class JobExecutorImpl implements JobService {
    protected final AppProperties appProperties;
    protected final MailService mailService;
    @Autowired
    private JobLogService jobLogService;
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    private Environment environment;

    protected JobExecutorImpl(AppProperties appProperties, MailService mailService) {
        this.appProperties = appProperties;
        this.mailService = mailService;

    }

    @Override
    public JobExecutionResults executeJob(String locationId) {
        log.info("Try to Start " + getJobName());
        JobExecutionResults jobExecutionResults = new JobExecutionResults();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            if (lock.tryLock()) {
                log.info(getJobName() + " Started");
                jobExecutionResults.setStartTime(startTime);
                String jobExecutionResult = execute(locationId);
                jobExecutionResults.setMessage(jobExecutionResult);
                jobExecutionResults.setJobName(getJobName());
                setEndingTimeAndDuration(jobExecutionResults, startTime);
                saveJobLog(jobExecutionResults);
            } else {
                log.info(getJobName() + " -> Already Running");
                jobExecutionResults.setMessage(getJobName() + " -> Already Running");
            }
        } catch (Exception ex) {
            setEndingTimeAndDuration(jobExecutionResults, startTime);
            jobExecutionResults.setError(ex.getMessage());
            jobExecutionResults.setStackTrace(Arrays.toString(ex.getStackTrace()));
            log.info(String.format("Job [%s] failed -> Send Mail with the reason", getJobName()));
            ex.printStackTrace();
        } finally {
            lock.unlock();
            sendMailOnFail(getJobName(), jobExecutionResults);
        }
        return jobExecutionResults;
    }

    private void saveJobLog(JobExecutionResults jobExecutionResult) {
        JobLog jobLog = new JobLog();
        jobLog.setDate(Timestamp.valueOf(jobExecutionResult.getStartTime()));
        jobLog.setJobName(jobExecutionResult.getJobName());
        jobLog.setComments(StringUtils.isEmpty(jobExecutionResult.getError()) ? jobExecutionResult.getMessage() : jobExecutionResult.getStackTrace());
        jobLog.setLastRun(jobExecutionResult.getEndTime().toLocalDate());
        jobLog.setStatus(getStatusFromJob(jobExecutionResult));
        jobLogService.saveJobLog(jobLog);
    }

    private String getStatusFromJob(JobExecutionResults jobExecutionResult) {
        if (StringUtils.isEmpty(jobExecutionResult.getError()))
            return "completed";
        else
            return jobExecutionResult.getError();
    }

    protected abstract String getJobName();

    protected abstract String execute(String locationId) throws Exception;


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
        String[] activeProfile = environment.getActiveProfiles();
        String activeProfileStr = String.join(",", activeProfile);
        String message = activeProfileStr + " " + (StringUtils.isEmpty(jobExecutionResults.getMessage()) ? "" : jobExecutionResults.getMessage());
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("START_TIME", jobExecutionResults.getStartTimeBeautify());
        contentTemplate.put("END_TIME", jobExecutionResults.getEndTimeBeautify());
        contentTemplate.put("ELAPSED_TIME", jobExecutionResults.getElapsedTime());
        contentTemplate.put("JOB_RESULT", message);
        contentTemplate.put("ERROR", StringUtils.isEmpty(jobExecutionResults.getError()) ? "" : jobExecutionResults.getError());
        contentTemplate.put("STACK_TRACE", StringUtils.isEmpty(jobExecutionResults.getStackTrace()) ? "" : jobExecutionResults.getStackTrace());
        return contentTemplate;
    }

}
