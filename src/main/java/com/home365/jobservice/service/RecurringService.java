package com.home365.jobservice.service;

import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.model.JobExecutionResults;

import java.util.List;

public interface RecurringService {
    List<Recurring> findByActive(boolean isActive);
    JobExecutionResults createTransactionsForRecurringCharges();
}
