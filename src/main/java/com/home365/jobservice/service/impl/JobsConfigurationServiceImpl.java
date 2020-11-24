package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;
import com.home365.jobservice.repository.JobsConfigurationRepository;
import com.home365.jobservice.service.JobsConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobsConfigurationServiceImpl implements JobsConfigurationService {

    private final JobsConfigurationRepository jobsConfigurationRepository;
    private final ObjectMapper objectMapper;

    public JobsConfigurationServiceImpl(JobsConfigurationRepository jobsConfigurationRepository,
                                        ObjectMapper objectMapper) {
        this.jobsConfigurationRepository = jobsConfigurationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public LateFeeConfiguration getLateFeeConfiguration() throws JsonProcessingException {
        JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LATE_FEE.value);
        return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LateFeeConfiguration.class);
    }

    @Override
    public LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration() throws JsonProcessingException {
        JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LEASE_PROPERTY_NOTIFICATION.value);
        return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LeasePropertyNotificationConfiguration.class);
    }

    private enum JOBS_ID {
        LATE_FEE(1L),
        LEASE_PROPERTY_NOTIFICATION(2L);

        private final Long value;

        JOBS_ID(long value) {
            this.value = value;
        }

        public Long getValue() {
            return value;
        }
    }
}
