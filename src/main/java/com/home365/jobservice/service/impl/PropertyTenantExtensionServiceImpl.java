package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.repository.IPropertyTenantExtensionRepository;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyTenantExtensionServiceImpl implements IPropertyTenantExtensionService {
    private final IPropertyTenantExtensionRepository propertyTenantExtensionRepository;

    public PropertyTenantExtensionServiceImpl(IPropertyTenantExtensionRepository propertyTenantExtensionRepository) {
        this.propertyTenantExtensionRepository = propertyTenantExtensionRepository;
    }

    @Override
    public List<PropertyTenantExtension> findAllByIds(List<String> ids) {
        return propertyTenantExtensionRepository.findAllById(ids);
    }

    @Override
    public void save(List<PropertyTenantExtension> propertyTenantExtensions) {
        propertyTenantExtensionRepository.saveAll(propertyTenantExtensions);
    }

    @Override
    public List<PropertyTenantExtension> getAllActivePlansToUpdate(String locationId) {
        return propertyTenantExtensionRepository.getAllActivePlansToUpdate(locationId);
    }
}
