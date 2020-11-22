package com.home365.jobservice.service;

import com.home365.jobservice.entities.RecurrentPropertyTenantProjection;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.model.JobExecutionResults;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RecurringService {
    List<Recurring> findByActive(boolean isActive);

    Optional<Recurring> findById(String recurringId);

    Recurring save(Recurring recurring);

    JobExecutionResults createTransactionsForRecurringCharges();

    List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate);

    List<RecurrentPropertyTenantProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds);
}
