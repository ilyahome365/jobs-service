package com.home365.jobservice.service;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.model.JobExecutionResults;

import java.util.List;

public interface ApplicationService {
   List<Transactions> pendingStatusChange();

    JobExecutionResults startLateFeeJob();

    JobExecutionResults createTransactionsForRecurringCharges();

    JobExecutionResults startLeasePropertyNotification();

    JobExecutionResults dueDateTenantNotification();
}
