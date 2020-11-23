package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;
import com.home365.jobservice.repository.PropertyRepository;
import com.home365.jobservice.service.PropertyService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
}
