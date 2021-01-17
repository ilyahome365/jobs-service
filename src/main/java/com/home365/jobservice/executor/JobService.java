package com.home365.jobservice.executor;

import com.home365.jobservice.model.JobExecutionResults;

public interface JobService {
    JobExecutionResults executeJob(String locationId);
}
