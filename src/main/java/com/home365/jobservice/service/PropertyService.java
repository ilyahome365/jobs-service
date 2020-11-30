package com.home365.jobservice.service;

import com.home365.jobservice.entities.IPlanInformation;
import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;

import java.util.Date;
import java.util.List;

public interface PropertyService {
    List<IPropertyLeaseInformationProjection> findAllForLeaseNotification(Date start, Date end);

    List<IPlanInformation> getAllActivePlans();
}
