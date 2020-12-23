package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.repository.JobLogRepository;
import com.home365.jobservice.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JobLogServiceImpl implements JobLogService {
    private final JobLogRepository jobLogRepository;


    public JobLogServiceImpl(JobLogRepository jobLogRepository) {
        this.jobLogRepository = jobLogRepository;

    }

    @Override
    public JobLog saveJobLog(JobLog jobLog) {
        log.info("save Job Log : {} ", jobLog);
        return jobLogRepository.save(jobLog);
    }


}
