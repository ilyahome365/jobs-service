package com.home365.jobservice.service.impl;

import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {


    private final LateFeeJobServiceImpl lateFeeJobService;
    private final LeaseRecurringNotificationServiceImpl leaseRecurringNotificationService;
    private final ChangeBillStatusServiceImpl changeBillStatusService;
    private final RecurringServiceImpl recurringService;
    private final DueDateNotificationServiceImpl dueDateNotificationService;
    private final LeaseUpdatingServiceImpl leaseUpdatingService;
    private final PhaseOutPropertyServiceImpl phaseOutPropertyService;
    private final OwnerNotificationsServiceImpl ownerNotificationsService;
    private final ActivateOwnerServiceImpl activateOwnerService;
    private final CreateWelcomeCreditServiceImpl createWelcomeCreditService;

    public ApplicationServiceImpl(ChangeBillStatusServiceImpl changeBillStatusService, RecurringServiceImpl recurringService,
                                  LateFeeJobServiceImpl lateFeeJobService,
                                  LeaseRecurringNotificationServiceImpl leaseRecurringNotificationService,
                                  DueDateNotificationServiceImpl dueDateNotificationService,
                                  LeaseUpdatingServiceImpl leaseUpdatingService, PhaseOutPropertyServiceImpl phaseOutPropertyService, OwnerNotificationsServiceImpl ownerNotificationsService, ActivateOwnerServiceImpl activateOwnerService, CreateWelcomeCreditServiceImpl createWelcomeCreditService) {

        this.changeBillStatusService = changeBillStatusService;
        this.lateFeeJobService = lateFeeJobService;
        this.recurringService = recurringService;
        this.leaseRecurringNotificationService = leaseRecurringNotificationService;
        this.dueDateNotificationService = dueDateNotificationService;
        this.leaseUpdatingService = leaseUpdatingService;
        this.phaseOutPropertyService = phaseOutPropertyService;
        this.ownerNotificationsService = ownerNotificationsService;
        this.activateOwnerService = activateOwnerService;
        this.createWelcomeCreditService = createWelcomeCreditService;
    }


    @Override
    public JobExecutionResults activateOwners(){
        return activateOwnerService.executeJob(null);
    }

    @Override
    public JobExecutionResults  createWelcomeCredit(){
        return createWelcomeCreditService.executeJob(null);
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
    public JobExecutionResults startOwnerRentNotification(String locationId) {
        return ownerNotificationsService.executeJob(locationId);
    }

    @Override
    public JobExecutionResults createTransactionsForRecurringCharges(String locationId) {
        return recurringService.executeJob(locationId);
    }

}
