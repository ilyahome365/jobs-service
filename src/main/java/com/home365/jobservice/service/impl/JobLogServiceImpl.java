package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.JobLogTest;
import com.home365.jobservice.repository.JobLogRepository;
import com.home365.jobservice.repository.JobLogTestRepository;
import com.home365.jobservice.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JobLogServiceImpl implements JobLogService {
    private final JobLogRepository jobLogRepository;
    private final JobLogTestRepository jobLogTestRepository;

    public JobLogServiceImpl(JobLogRepository jobLogRepository, JobLogTestRepository jobLogTestRepository) {
        this.jobLogRepository = jobLogRepository;
        this.jobLogTestRepository = jobLogTestRepository;
    }

    @Override
    public JobLog saveJobLog(JobLog jobLog) {
        log.info("save Job Log : {} ", jobLog);
        return jobLogRepository.save(jobLog);
    }

    @Override
    public JobLogTest saveJobLogTest(JobLogTest jobLogTest) {
        return jobLogTestRepository.save(jobLogTest);
    }
}
