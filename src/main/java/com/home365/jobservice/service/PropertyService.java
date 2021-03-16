package com.home365.jobservice.service;

import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PropertyService {
    List<IPropertyLeaseInformationProjection> findAllForLeaseNotification(Date start, Date end);
    Optional<PropertyExtension> findPropertyById(String propertyId);
}
