package com.home365.jobservice.service;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.JobLogTest;

public interface JobLogService {
    JobLog saveJobLog(JobLog jobLog);

    JobLogTest saveJobLogTest(JobLogTest jobLogTest);
}
