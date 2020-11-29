package com.home365.jobservice.config;

import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.JobInfo;
import com.home365.jobservice.service.ApplicationService;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.impl.JobsConfigurationServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Slf4j
@Data
@Configuration
public class AppConfiguration implements SchedulingConfigurer {

    private final ApplicationService applicationService;
    private final JobsConfigurationService jobsConfigurationService;
    private final Map<String, ScheduledFutureWrapper> jobNameToJob;
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    public AppConfiguration(ApplicationService applicationService,
                            JobsConfigurationService jobsConfigurationService) {
        this.applicationService = applicationService;
        this.jobsConfigurationService = jobsConfigurationService;
        this.jobNameToJob = new HashMap<>();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        if (this.scheduledTaskRegistrar == null) {
            this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        }
        if (this.scheduledTaskRegistrar.getScheduler() == null) {
            this.scheduledTaskRegistrar.setScheduler(configurePool());
        }

        addJob(JobsConfigurationServiceImpl.JOBS_ID.LATE_FEE.getName(), () -> log.info("------------------------> RUN LATE_FEE JOB <------------------------"));
        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_PROPERTY_NOTIFICATION.getName(), () -> log.info("------------------------> RUN LEASE_PROPERTY_NOTIFICATION JOB <------------------------"));

//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LATE_FEE.getName(),applicationService::startLateFeeJob);
//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_PROPERTY_NOTIFICATION.getName(),applicationService::startLeasePropertyNotification);
    }

    @Bean
    public ThreadPoolTaskScheduler configurePool() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("my-scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    public List<JobInfo> getAllJobs() {
        return jobNameToJob.values()
                .stream()
                .map(ScheduledFutureWrapper::getJobInfo)
                .collect(Collectors.toList());
    }

    public synchronized void addJob(String jobName, Runnable task) {
        boolean exist = jobNameToJob.containsKey(jobName);
        if (exist) {
            log.info("Job with name [%s] already exist -> please cancel the existing Job and try again");
            return;
        }
        ScheduledFutureWrapper scheduledFutureWrapper = new ScheduledFutureWrapper(jobName, task);
        startJob(scheduledFutureWrapper);
        jobNameToJob.put(jobName, scheduledFutureWrapper);
    }

    public synchronized void removeJob(String jobName) {
        ScheduledFutureWrapper scheduledFutureWrapper = jobNameToJob.get(jobName);
        scheduledFutureWrapper.stopJob(true);
        scheduledFutureWrapper.setScheduledFuture(null);
        jobNameToJob.remove(jobName);
    }

    public synchronized void restartJob(String jobName) {
        ScheduledFutureWrapper scheduledFutureWrapper = jobNameToJob.get(jobName);
        scheduledFutureWrapper.stopJob(true);
        startJob(scheduledFutureWrapper);
    }

    public synchronized void startJob(String jobName) {
        ScheduledFutureWrapper scheduledFutureWrapper = jobNameToJob.get(jobName);
        startJob(scheduledFutureWrapper);
    }

    public synchronized void stopJob(String jobName) {
        ScheduledFutureWrapper scheduledFutureWrapper = jobNameToJob.get(jobName);
        scheduledFutureWrapper.stopJob(true);
    }

    public synchronized void restartAll() {
        stopAll();
        startAll();
    }

    public synchronized void startAll() {
        jobNameToJob
                .values()
                .stream()
                .filter(ScheduledFutureWrapper::isNotActive)
                .forEach(this::startJob);
    }

    public synchronized void stopAll() {
        jobNameToJob.values().forEach(scheduledFutureWrapper -> scheduledFutureWrapper.stopJob(true));
    }

    private void startJob(ScheduledFutureWrapper scheduledFutureWrapper) {
        if (scheduledFutureWrapper == null) {
            log.error("Unable to find Job -> please add the job");
            return;
        }

        Optional<JobConfiguration> jobConfigurationOptional = jobsConfigurationService.getJobByName(scheduledFutureWrapper.getName());
        if (jobConfigurationOptional.isEmpty()) {
            log.error(String.format("Unable to find Job configuration with name [%s] -> please verify JobConfiguration is added to DB", scheduledFutureWrapper.getName()));
            return;
        }
        JobConfiguration jobConfiguration = jobConfigurationOptional.get();

        if (scheduledTaskRegistrar.getScheduler() == null) {
            scheduledTaskRegistrar.setScheduler(configurePool());
        }
        String cron = jobConfiguration.getCron();

        log.info(String.format("Start Job [%s] Next execution time of this taken from cron expression -> [%s]", scheduledFutureWrapper.getName(), cron));
        ScheduledFuture<?> task = scheduledTaskRegistrar
                .getScheduler()
                .schedule(scheduledFutureWrapper.getTask(), new CronTrigger(cron, TimeZone.getDefault()));
        scheduledFutureWrapper.setCrone(cron);
        scheduledFutureWrapper.setActive(true);
        scheduledFutureWrapper.setScheduledFuture(task);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduledFutureWrapper {
        private boolean active;
        private String name;
        private String crone;
        private ScheduledFuture<?> scheduledFuture;
        private Runnable task;

        public ScheduledFutureWrapper(String name, Runnable task) {
            this.name = name;
            this.task = task;
        }

        public void stopJob(boolean mayInterruptIfRunning) {
            log.info("Cancel Job: " + name);
            scheduledFuture.cancel(mayInterruptIfRunning);
            active = false;
        }

        public boolean isActive() {
            return scheduledFuture != null && !scheduledFuture.isCancelled() && active;
        }

        public boolean isNotActive() {
            return scheduledFuture.isCancelled() && !active;
        }

        public JobInfo getJobInfo() {
            JobInfo jobInfo = new JobInfo();
            jobInfo.setActive(isActive());
            jobInfo.setTaskName(getName());
            jobInfo.setCrone(getCrone());
            return jobInfo;
        }
    }
}
