package com.home365.jobservice.service;

import com.home365.jobservice.entities.PropertyTenantExtension;

import java.util.List;

public interface IPropertyTenantExtensionService {
    List<PropertyTenantExtension> findAllByIds(List<String> ids);

    void save(List<PropertyTenantExtension> allLeaseToExtend);

    List<PropertyTenantExtension> getAllActivePlansToUpdate(String locationId);
}
