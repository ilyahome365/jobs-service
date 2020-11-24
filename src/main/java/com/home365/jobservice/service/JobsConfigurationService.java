package com.home365.jobservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;

public interface JobsConfigurationService {
    LateFeeConfiguration getLateFeeConfiguration() throws JsonProcessingException;

    LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration() throws JsonProcessingException;
}
