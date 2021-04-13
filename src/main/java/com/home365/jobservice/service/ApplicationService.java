package com.home365.jobservice.service;

import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.model.JobExecutionResults;

import java.util.List;

public interface ApplicationService {
//   List<TransactionsWithProjectedBalance> pendingStatusChange();

    JobExecutionResults startLateFeeJob(String locationId);

    JobExecutionResults createTransactionsForRecurringCharges(String locationId);

    JobExecutionResults startLeasePropertyNotification(String locationId);

    JobExecutionResults dueDateTenantNotification(String locationId);

    JobExecutionResults startLeaseUpdating(String locationId);

    JobExecutionResults startChangeBillStatusJob(String locationId);
    JobExecutionResults startPhaseOutProperty(String locationId);

    JobExecutionResults startOwnerRentNotification(String locationId);


}
