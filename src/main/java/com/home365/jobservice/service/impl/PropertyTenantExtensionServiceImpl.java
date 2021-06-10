package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.repository.IPropertyTenantExtensionRepository;
import com.home365.jobservice.service.FindByIdAudit;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyTenantExtensionServiceImpl implements IPropertyTenantExtensionService, FindByIdAudit {



    private final IPropertyTenantExtensionRepository propertyTenantExtensionRepository;

    private final   FindByIdAudit findByIdAudit;



    public PropertyTenantExtensionServiceImpl(IPropertyTenantExtensionRepository propertyTenantExtensionRepository, FindByIdAudit findByIdAudit) {
        this.propertyTenantExtensionRepository = propertyTenantExtensionRepository;
        this.findByIdAudit = findByIdAudit;
        this.findByIdAudit.setRepository(propertyTenantExtensionRepository);
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

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        return this.findByIdAudit.findByIdAudit(newEntity);
    }

    @Override
    public List<IAuditableEntity> findByList(List<IAuditableEntity> entityList) {
        return this.findByIdAudit.findByList(entityList);
    }

    @Override
    public void setRepository(JpaRepository repository) {
        this.findByIdAudit.setRepository(repository);
    }
}
