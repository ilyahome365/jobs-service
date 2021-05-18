package com.home365.jobservice.service;

import com.home365.jobservice.model.JobExecutionResults;

public interface ApplicationService {

    JobExecutionResults activateOwners();

    JobExecutionResults  createWelcomeCredit();

    JobExecutionResults startLateFeeJob(String locationId);

    JobExecutionResults createTransactionsForRecurringCharges(String locationId);

    JobExecutionResults startLeasePropertyNotification(String locationId);

    JobExecutionResults sendReminderOfFirstContribution();

    JobExecutionResults dueDateTenantNotification(String locationId);

    JobExecutionResults startLeaseUpdating(String locationId);

    JobExecutionResults startChangeBillStatusJob(String locationId);

    JobExecutionResults startPhaseOutProperty(String locationId);

    JobExecutionResults startOwnerRentNotification(String locationId);
}
