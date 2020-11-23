package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;
import com.home365.jobservice.repository.JobsConfigurationRepository;
import com.home365.jobservice.service.JobsConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class JobsConfigurationServiceImpl implements JobsConfigurationService {

    private final Long lateFeeId = 1L;

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
            JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LATE_FEE.value);
            return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LateFeeConfiguration.class);
        } catch (Exception e) {
            log.error("Unable to parse Late Fee Configuration JSON using default values as specify in " + LateFeeConfiguration.class.getName());
            log.warn(e.getMessage());
            log.warn(Arrays.toString(e.getStackTrace()));
        }
        return new LateFeeConfiguration();
    }

    @Override
    public LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration() {
        try {
            JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LEASE_PROPERTY_NOTIFICATION.value);
            return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LeasePropertyNotificationConfiguration.class);
        } catch (Exception e) {
            log.error("Unable to parse Lease Property Configuration JSON using default values as specify in " + LeasePropertyNotificationConfiguration.class);
            log.warn(e.getMessage());
            log.warn(Arrays.toString(e.getStackTrace()));
        }
        return new LeasePropertyNotificationConfiguration();
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
