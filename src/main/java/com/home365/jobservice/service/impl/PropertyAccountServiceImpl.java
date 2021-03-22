package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.PropertyAccountExtension;
import com.home365.jobservice.repository.PropertyAccountRepository;
import com.home365.jobservice.service.PropertyAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PropertyAccountServiceImpl implements PropertyAccountService {
    private final PropertyAccountRepository propertyAccountRepository;

    public PropertyAccountServiceImpl(PropertyAccountRepository propertyAccountRepository) {
        this.propertyAccountRepository = propertyAccountRepository;
    }

    @Override
    public List<PropertyAccountExtension> getByPropertyId(String propertyId) {
        log.debug("Get by property id :{} ", propertyId);
        return propertyAccountRepository.findByPropertyId(propertyId);
    }
}
