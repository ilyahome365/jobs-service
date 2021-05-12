package com.home365.jobservice.config;

import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.jobs.JobInfo;
import com.home365.jobservice.model.jobs.JobScheduledWrapper;
import com.home365.jobservice.model.jobs.LocationJobsInfo;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.impl.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static com.home365.jobservice.config.ThreadPool.configurePool;

@Slf4j
@Data
@Service
public class AppConfiguration implements SchedulingConfigurer {

    private final LeaseUpdatingServiceImpl leaseUpdatingService;
    private final ChangeBillStatusServiceImpl changeBillStatusService;
    private final JobsConfigurationService jobsConfigurationService;
    private final DueDateNotificationServiceImpl dueDateNotificationService;
    private final PhaseOutPropertyServiceImpl phaseOutPropertyService;
    private final OwnerNotificationsServiceImpl ownerNotificationsService;
    private final ActivateOwnerServiceImpl activateOwnerService;

    private final Map<String, Map<String, JobScheduledWrapper>> jobLocationToJob;
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    private final ApplicationContext context;


    public AppConfiguration(LeaseUpdatingServiceImpl leaseUpdatingService, DueDateNotificationServiceImpl dueDateNotificationService, ApplicationContext context,
                            ChangeBillStatusServiceImpl changeBillStatusService, JobsConfigurationService jobsConfigurationService, PhaseOutPropertyServiceImpl phaseOutPropertyService, OwnerNotificationsServiceImpl ownerNotificationsService, ActivateOwnerServiceImpl activateOwnerService) {
        this.leaseUpdatingService = leaseUpdatingService;
        this.changeBillStatusService = changeBillStatusService;
        this.jobsConfigurationService = jobsConfigurationService;
        this.dueDateNotificationService = dueDateNotificationService;
        this.phaseOutPropertyService = phaseOutPropertyService;
        this.ownerNotificationsService = ownerNotificationsService;
        this.activateOwnerService = activateOwnerService;
        this.jobLocationToJob = new HashMap<>();
        this.context = context;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        if (this.scheduledTaskRegistrar == null) {
            this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        }
        if (this.scheduledTaskRegistrar.getScheduler() == null) {
            this.scheduledTaskRegistrar.setScheduler(configurePool());
        }

        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_UPDATING.getName(),
                Constants.LV_PM_ACCOUNT,
                () -> leaseUpdatingService.executeJob(Constants.LV_PM_ACCOUNT)
        );
        addJob(JobsConfigurationServiceImpl.JOBS_ID.LEASE_UPDATING.getName(),
                Constants.AT_PM_ACCOUNT,
                () -> leaseUpdatingService.executeJob(Constants.AT_PM_ACCOUNT)
        );
// phsae out property
        addJob(JobsConfigurationServiceImpl.JOBS_ID.PHASE_OUT_PROPERTY.getName(),
                Constants.LV_PM_ACCOUNT,
                () -> phaseOutPropertyService.executeJob(Constants.LV_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.PHASE_OUT_PROPERTY.getName(),
                Constants.AT_PM_ACCOUNT,
                () -> phaseOutPropertyService.executeJob(Constants.AT_PM_ACCOUNT)
        );

//         change bill status
        addJob(JobsConfigurationServiceImpl.JOBS_ID.CHANGE_BILL_STATUS.getName(),
                Constants.LV_PM_ACCOUNT,
                () -> changeBillStatusService.executeJob(Constants.LV_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.CHANGE_BILL_STATUS.getName(),
                Constants.AT_PM_ACCOUNT,
                () -> changeBillStatusService.executeJob(Constants.AT_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.DUE_DATE_NOTIFICATION.getName(),
                Constants.LV_PM_ACCOUNT,
                () -> dueDateNotificationService.executeJob(Constants.LV_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.OWNER_RENT_NOTIFICATION.getName(),
                Constants.LV_PM_ACCOUNT,
                () -> ownerNotificationsService.executeJob(Constants.LV_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.OWNER_RENT_NOTIFICATION.getName(),
                Constants.AT_PM_ACCOUNT,
                () -> ownerNotificationsService.executeJob(Constants.AT_PM_ACCOUNT)
        );

        addJob(JobsConfigurationServiceImpl.JOBS_ID.OWNER_RENT_NOTIFICATION.getName(), Constants.LV_PM_ACCOUNT, () -> activateOwnerService
                .executeJob(null));
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
            log.error("Unable to find Job configuration with location {} name {} -> please verify JobConfiguration is added to DB", location, jobName);
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
            log.info(Constants.UNABLE_TO_FIND_LOCATION, location);
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
            log.info(Constants.UNABLE_TO_FIND_LOCATION, location);
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
            log.info(Constants.UNABLE_TO_FIND_LOCATION, location);
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
            log.info(Constants.UNABLE_TO_FIND_LOCATION, location);
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
            log.error("Unable to find Job configuration with location {} name {} -> please verify JobConfiguration is added to DB",
                    scheduledFutureWrapper.getLocation(),
                    scheduledFutureWrapper.getName());
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

        if (scheduledTaskRegistrar.getScheduler() != null) {
            ScheduledFuture<?> task = scheduledTaskRegistrar
                    .getScheduler()
                    .schedule(scheduledFutureWrapper.getTask(), scheduledFutureWrapper.getTrigger());
            scheduledFutureWrapper.setScheduledFuture(task);
        }
    }

}
