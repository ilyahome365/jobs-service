package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import com.home365.jobservice.repository.PropertyRepository;
import com.home365.jobservice.service.PropertyService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public List<IPropertyLeaseInformationProjection> findAllForLeaseNotification(Date start, Date end) {
        return propertyRepository.findAllForLeaseNotification(start, end);
    }

    @Override
    public Optional<PropertyExtension> findPropertyById(String propertyId) {
        return propertyRepository.findById(propertyId);
    }
}
