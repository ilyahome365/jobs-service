package com.home365.jobservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.home365.jobservice.entities.ChangeBillConfiguration;
import com.home365.jobservice.entities.JobConfiguration;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;

import java.util.Optional;

public interface JobsConfigurationService {
    LateFeeConfiguration getLateFeeConfiguration() throws JsonProcessingException;

    LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration() throws JsonProcessingException;

    Optional<JobConfiguration> getJobByName(String taskName);

    Optional<JobConfiguration> getJobByLocationAndName(String location, String jobName);
    ChangeBillConfiguration getChangeBillConfiguration();


}
