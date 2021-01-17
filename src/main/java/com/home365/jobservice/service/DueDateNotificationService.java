package com.home365.jobservice.service;

import com.home365.jobservice.model.JobExecutionResults;

public interface DueDateNotificationService {

    JobExecutionResults sendNotificationForDueDateTenants(String locationId);
}
