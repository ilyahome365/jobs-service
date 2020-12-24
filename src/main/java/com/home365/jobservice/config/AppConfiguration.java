package com.home365.jobservice.config;

import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.jobs.JobInfo;
import com.home365.jobservice.model.jobs.JobScheduledWrapper;
import com.home365.jobservice.model.jobs.LocationJobsInfo;
import com.home365.jobservice.service.ApplicationService;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.impl.JobsConfigurationServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Slf4j
@Data
@Configuration
public class AppConfiguration implements SchedulingConfigurer {

    private final ApplicationService applicationService;
    private final JobsConfigurationService jobsConfigurationService;

    // <location, <jobName,jobObject>>
    private final Map<String, Map<String, JobScheduledWrapper>> jobLocationToJob;
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    public AppConfiguration(ApplicationService applicationService,
                            JobsConfigurationService jobsConfigurationService) {
        this.applicationService = applicationService;
        this.jobsConfigurationService = jobsConfigurationService;
        this.jobLocationToJob = new HashMap<>();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        if (this.scheduledTaskRegistrar == null) {
            this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        }
        if (this.scheduledTaskRegistrar.getScheduler() == null) {
            this.scheduledTaskRegistrar.setScheduler(configurePool());
        }


//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LATE_FEE.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
//                () -> log.info("------------------------> RUN LATE_FEE JOB <------------------------")
//        );

//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LATE_FEE.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
//                () -> log.info("------------------------> RUN LATE_FEE JOB <------------------------")
//        );
//
//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_PROPERTY_NOTIFICATION.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
//                () -> log.info("------------------------> RUN LEASE_PROPERTY_NOTIFICATION JOB <------------------------")
//        );
//
        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_UPDATING.getName(),
                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
                () -> applicationService.startLeaseUpdating("F90E128A-CD00-4DF7-B0D0-0F40F80D623A")
        );

//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LATE_FEE.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
////                applicationService::startLateFeeJob
//                ()-> applicationService.startLateFeeJob()
//        );
//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_PROPERTY_NOTIFICATION.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
//                applicationService::startLeasePropertyNotification
//        );
//        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_UPDATING.getName(),
//                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
//              ()->applicationService.startLeaseUpdating("F90E128A-CD00-4DF7-B0D0-0F40F80D623A")
//        );
        addJob(JobsConfigurationServiceImpl.JOBS_ID.CHANGE_BILL_STATUS.getName(),
                "F90E128A-CD00-4DF7-B0D0-0F40F80D623A",
              ()->applicationService.startChangeBillStatusJob("F90E128A-CD00-4DF7-B0D0-0F40F80D623A")
        );


    }

    @Bean
    public ThreadPoolTaskScheduler configurePool() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("my-scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    public List<LocationJobsInfo> getAllJobs() {
        List<LocationJobsInfo> locationJobsInfos = new ArrayList<>();
        jobLocationToJob.forEach((location, value) -> {

            LocationJobsInfo locationJobsInfo = new LocationJobsInfo();
            locationJobsInfo.setLocation(location);

            List<JobInfo> jobs = value
                    .values()
                    .stream()
                    .map(JobScheduledWrapper::getJobInfo).collect(Collectors.toList());
            locationJobsInfo.setJobsInfo(jobs);

            locationJobsInfos.add(locationJobsInfo);
        });
        return locationJobsInfos;
    }

    public synchronized void addJob(String jobName, String location, Runnable task) {
        Optional<JobConfiguration> jobConfigurationOptional = jobsConfigurationService.getJobByLocationAndName(location, jobName);
        if (jobConfigurationOptional.isEmpty()) {
            log.error(String.format("Unable to find Job configuration with location [%s] name [%s] -> please verify JobConfiguration is added to DB", location, jobName));
            return;
        }
        JobConfiguration jobConfiguration = jobConfigurationOptional.get();

        Map<String, JobScheduledWrapper> locationJobs = jobLocationToJob.computeIfAbsent(location, k -> new HashMap<>());

        JobScheduledWrapper scheduledFutureWrapper = new JobScheduledWrapper(jobName, location, jobConfiguration.getCron(), task);
        startJob(scheduledFutureWrapper);
        locationJobs.put(scheduledFutureWrapper.getName(), scheduledFutureWrapper);
    }

    public synchronized void removeJob(String location, String jobName) {
        Map<String, JobScheduledWrapper> locationJobs = jobLocationToJob.get(location);
        if (locationJobs == null) {
            log.info("Unable to find Location: " + location);
            return;
        }
        JobScheduledWrapper jobScheduledWrapper = locationJobs.get(jobName);
        if (jobScheduledWrapper != null) {
            jobScheduledWrapper.stopJob(true);
            jobScheduledWrapper.setScheduledFuture(null);
            locationJobs.remove(jobName);
        }
    }

    public synchronized void restartJob(String location, String jobName) {
        Map<String, JobScheduledWrapper> locationJobs = jobLocationToJob.get(location);
        if (locationJobs == null) {
            log.info("Unable to find Location: " + location);
            return;
        }
        JobScheduledWrapper jobScheduledWrapper = locationJobs.get(jobName);
        if (jobScheduledWrapper != null) {
            jobScheduledWrapper.stopJob(true);
            refreshJob(jobScheduledWrapper);
            startJob(jobScheduledWrapper);
        }
    }

    public synchronized void startJob(String location, String jobName) {
        Map<String, JobScheduledWrapper> locationJobs = jobLocationToJob.get(location);
        if (locationJobs == null) {
            log.info("Unable to find Location: " + location);
            return;
        }
        JobScheduledWrapper jobScheduledWrapper = locationJobs.get(jobName);
        if (jobScheduledWrapper != null) {
            startJob(jobScheduledWrapper);
        }
    }

    public synchronized void stopJob(String location, String jobName) {
        Map<String, JobScheduledWrapper> locationJobs = jobLocationToJob.get(location);
        if (locationJobs == null) {
            log.info("Unable to find Location: " + location);
            return;
        }
        JobScheduledWrapper jobScheduledWrapper = locationJobs.get(jobName);
        if (jobScheduledWrapper != null) {
            jobScheduledWrapper.stopJob(true);
        }
    }


    public synchronized void restartAll() {
        stopAll();
        refreshAll();
        startAll();
    }

    public synchronized void startAll() {
        jobLocationToJob.values().forEach(location -> location.values().stream().filter(JobScheduledWrapper::isNotActive).forEach(AppConfiguration.this::startJob));
    }

    private void refreshAll() {
        jobLocationToJob.values().forEach(location -> location.values().forEach(AppConfiguration.this::refreshJob));
    }

    public synchronized void stopAll() {
        jobLocationToJob.values().forEach(location -> location.values().forEach(jobScheduledWrapper -> jobScheduledWrapper.stopJob(true)));
    }

    private void refreshJob(JobScheduledWrapper scheduledFutureWrapper) {
        Optional<JobConfiguration> jobConfigurationOptional = jobsConfigurationService.getJobByLocationAndName(
                scheduledFutureWrapper.getLocation(),
                scheduledFutureWrapper.getName()
        );
        if (jobConfigurationOptional.isEmpty()) {
            log.error(String.format("Unable to find Job configuration with location [%s] name [%s] -> please verify JobConfiguration is added to DB",
                    scheduledFutureWrapper.getLocation(),
                    scheduledFutureWrapper.getName())
            );
            return;
        }
        JobConfiguration jobConfiguration = jobConfigurationOptional.get();
        String cron = jobConfiguration.getCron();
        scheduledFutureWrapper.setCron(cron);
    }

    private void startJob(JobScheduledWrapper scheduledFutureWrapper) {
        if (scheduledFutureWrapper == null) {
            log.error("Unable to find Job -> please add the job");
            return;
        }

        if (scheduledTaskRegistrar.getScheduler() == null) {
            scheduledTaskRegistrar.setScheduler(configurePool());
        }

        log.info(String.format("Start Job [%s] in Location [%s] Next execution time of this taken from cron expression -> [%s]",
                scheduledFutureWrapper.getName(),
                scheduledFutureWrapper.getLocation(),
                scheduledFutureWrapper.getCron())
        );

        ScheduledFuture<?> task = scheduledTaskRegistrar
                .getScheduler()
                .schedule(scheduledFutureWrapper.getTask(), scheduledFutureWrapper.getTrigger());
        scheduledFutureWrapper.setScheduledFuture(task);
    }
}
