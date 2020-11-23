package com.home365.jobservice.service;

import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;

public interface JobsConfigurationService {
    LateFeeConfiguration getLateFeeConfiguration();

    LeasePropertyNotificationConfiguration getLeasePropertyNotificationConfiguration();
}
