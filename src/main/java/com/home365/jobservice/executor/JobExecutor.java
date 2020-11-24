package com.home365.jobservice.executor;

import com.home365.jobservice.model.JobExecutionResults;

public interface JobExecutor {
    JobExecutionResults executeJob(JobService jobService);
}
