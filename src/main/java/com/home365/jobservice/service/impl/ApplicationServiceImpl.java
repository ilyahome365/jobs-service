package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.service.ApplicationService;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.TransactionsLogService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final String JOB_PENDING_DUE = "ChangeBillStatusJob";


    private final AppProperties appProperties;

    private final JobLogService jobLogService;
    private final TransactionsLogService transactionsLogService;
    private final LateFeeJobServiceImpl lateFeeJobService;
    private final LeaseRecurringNotificationServiceImpl leaseRecurringNotificationService;
    private final ChangeBillStatusServiceImpl changeBillStatusService;
    private final RecurringServiceImpl recurringService;
    private final DueDateNotificationServiceImpl dueDateNotificationService;
    private final LeaseUpdatingServiceImpl leaseUpdatingService;
    private final PhaseOutPropertyServiceImpl phaseOutPropertyService;

    public ApplicationServiceImpl(AppProperties appProperties,
                                  TransactionsService transactionsService,
                                  JobLogService jobLogService,
                                  TransactionsLogService transactionsLogService,
                                  ChangeBillStatusServiceImpl changeBillStatusService, RecurringServiceImpl recurringService,
                                  LateFeeJobServiceImpl lateFeeJobService,
                                  LeaseRecurringNotificationServiceImpl leaseRecurringNotificationService,
                                  DueDateNotificationServiceImpl dueDateNotificationService,
                                  LeaseUpdatingServiceImpl leaseUpdatingService, PhaseOutPropertyServiceImpl phaseOutPropertyService) {
        this.appProperties = appProperties;

        this.jobLogService = jobLogService;
        this.transactionsLogService = transactionsLogService;
        this.changeBillStatusService = changeBillStatusService;
        this.lateFeeJobService = lateFeeJobService;
        this.recurringService = recurringService;
        this.leaseRecurringNotificationService = leaseRecurringNotificationService;
        this.dueDateNotificationService = dueDateNotificationService;
        this.leaseUpdatingService = leaseUpdatingService;
        this.phaseOutPropertyService = phaseOutPropertyService;
    }


    @Override
    public JobExecutionResults startLateFeeJob(String locationId) {
        return lateFeeJobService.executeJob(locationId);
    }


    @Override
    public JobExecutionResults startLeasePropertyNotification(String locationId) {
        return leaseRecurringNotificationService.executeJob(locationId);
    }

    @Override
    public JobExecutionResults dueDateTenantNotification(String locationId) {
        return dueDateNotificationService.executeJob(locationId);
    }

    @Override
    public JobExecutionResults startLeaseUpdating(String locationId) {
        return leaseUpdatingService.executeJob(locationId);
    }

    @Override
    public JobExecutionResults startChangeBillStatusJob(String locationId) {
        return changeBillStatusService.executeJob(locationId);

    }

    @Override
    public JobExecutionResults startPhaseOutProperty(String locationId) {

        return phaseOutPropertyService.executeJob(locationId);
    }

    @Override
    public JobExecutionResults createTransactionsForRecurringCharges(String locationId) {
        //return recurringService.createTransactionsForRecurringCharges(locationId);
        return recurringService.executeJob(locationId);
    }

    private String createNextCycleDate() {
        String dayOfNextCycle = "14";
        LocalDateTime now = LocalDateTime.now().plusMonths(1);
        String month = String.valueOf(now.getMonth().getValue());
        String year;
        if (now.getMonth().getValue() == 1)
            year = String.valueOf(now.getYear() + 1);
        else
            year = String.valueOf(now.getYear());
        return year + "-" + month + "-" + dayOfNextCycle;
    }
}
