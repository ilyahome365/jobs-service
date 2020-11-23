package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.repository.JobsConfigurationRepository;
import com.home365.jobservice.service.JobsConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class JobsConfigurationServiceImpl implements JobsConfigurationService {

    private final Long jobId = 1L;
    private final JobsConfigurationRepository jobsConfigurationRepository;
    private final ObjectMapper objectMapper;

    public JobsConfigurationServiceImpl(JobsConfigurationRepository jobsConfigurationRepository,
                                        ObjectMapper objectMapper) {
        this.jobsConfigurationRepository = jobsConfigurationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public LateFeeConfiguration getLateFeeConfiguration() {
        try {
            JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(jobId);
            return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LateFeeConfiguration.class);
        } catch (Exception e) {
            log.error("Unable to parse Late Fee Configuration JSON using default values");
            log.warn(e.getMessage());
            log.warn(Arrays.toString(e.getStackTrace()));
        }
        return new LateFeeConfiguration();
    }
}
