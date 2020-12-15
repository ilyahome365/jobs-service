package com.home365.jobservice.service;

import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.model.JobExecutionResults;

import java.util.List;

public interface ApplicationService {
   List<TransactionsWithProjectedBalance> pendingStatusChange();

    JobExecutionResults startLateFeeJob();

    JobExecutionResults createTransactionsForRecurringCharges();

    JobExecutionResults startLeasePropertyNotification();

    JobExecutionResults dueDateTenantNotification();

    JobExecutionResults startLeaseUpdating();

    JobExecutionResults startChangeBillStatusJob() throws Exception;


}
