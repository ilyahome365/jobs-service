package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.ChangeBillConfiguration;
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
    public LateFeeConfiguration getLateFeeConfiguration(String locationId) throws JsonProcessingException {
        Optional<JobConfiguration> jobConfiguration = jobsConfigurationRepository.findByLocationAndName(locationId, JOBS_ID.LATE_FEE.getName());
        return objectMapper.readValue(jobConfiguration.get().getConfigurationJson(), LateFeeConfiguration.class);
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

    @Override
    public Optional<JobConfiguration> getJobByLocationAndName(String location, String jobName) {
        return jobsConfigurationRepository.findByLocationAndName(location, jobName);
    }

    @Override
    public ChangeBillConfiguration getChangeBillConfiguration(String locationId) throws JsonProcessingException {
        Optional<JobConfiguration> jobConfiguration = getJobByLocationAndName(locationId, Constants.BILLS_STATUS_CHANGE);
        return objectMapper.readValue(jobConfiguration.get().getConfigurationJson(), ChangeBillConfiguration.class);
    }

    public enum JOBS_ID {
        LATE_FEE("late-fee", 1L),
        LEASE_PROPERTY_NOTIFICATION("lease-property-notification", 2L),
        LEASE_UPDATING("lease-updating", 3L),
        CHANGE_BILL_STATUS(Constants.BILLS_STATUS_CHANGE, 4L),
        DUE_DATE_NOTIFICATION("due-date-notification", 5L),
        PHASE_OUT_PROPERTY("phase-out-property", 6L),
        OWNER_RENT_NOTIFICATION("owner-rent-notification", 7L),
        ACTIVATE_OWNERS(Constants.ACTIVE_OWNER,8L),
        INSURANCE_PAY_BILLS(Constants.INSURANCE_PAY_BILLS, 9L),
        WELCOME_CREDIT(Constants.WELCOME_CREDIT,10L),
        REMINDER_CONTRIBUTION(Constants.REMINDER_FIRST_CONTRIBUTION,11L),
        CREATE_RECURRING_TRANSACTIONS("create-recurring-transactions", 15L);

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
