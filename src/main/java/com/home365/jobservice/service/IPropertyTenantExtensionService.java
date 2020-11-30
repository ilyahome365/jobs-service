package com.home365.jobservice.service;

import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.projection.ILeaseInformation;

import java.util.List;

public interface IPropertyTenantExtensionService {
    List<PropertyTenantExtension> findAllByIds(List<String> ids);

    void save(List<PropertyTenantExtension> allLeaseToExtend);

    List<ILeaseInformation> getAllActivePlans();

}
