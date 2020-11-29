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

import java.util.Optional;

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
        JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LATE_FEE.key);
        return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LateFeeConfiguration.class);
    }

    @Override
    public LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration() throws JsonProcessingException {
        JobConfiguration jobConfiguration = jobsConfigurationRepository.getOne(JOBS_ID.LEASE_PROPERTY_NOTIFICATION.key);
        return objectMapper.readValue(jobConfiguration.getConfigurationJson(), LeasePropertyNotificationConfiguration.class);
    }

    @Override
    public Optional<JobConfiguration> getJobByName(String taskName) {
        Optional<JOBS_ID> jobOptional = JOBS_ID.findIdByName(taskName);
        if (jobOptional.isEmpty()) {
            log.error(String.format("Unable to find JOB with name [%s] - > please add to JOBS_ID enum", taskName));
            return Optional.empty();
        }
        JOBS_ID job = jobOptional.get();
        return jobsConfigurationRepository.findById(job.getKey());
    }

    public enum JOBS_ID {
        LATE_FEE("late-fee", 1L),
        LEASE_PROPERTY_NOTIFICATION("lease-property-notification", 2L);

        private final String name;
        private final Long key;

        JOBS_ID(String name, long key) {
            this.name = name;
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public Long getKey() {
            return key;
        }

        public static Optional<JOBS_ID> findIdByName(String name) {
            for (JOBS_ID jobName : values()) {
                if (jobName.getName().equals(name)) {
                    return Optional.of(jobName);
                }
            }
            return Optional.empty();
        }
    }
}
