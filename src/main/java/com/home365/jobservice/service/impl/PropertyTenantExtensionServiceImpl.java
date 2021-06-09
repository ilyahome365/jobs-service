package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.repository.IPropertyTenantExtensionRepository;
import com.home365.jobservice.service.FindByIdAudit;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class PropertyTenantExtensionServiceImpl implements IPropertyTenantExtensionService, FindByIdAudit {

    @PersistenceContext
    private EntityManager entityManager;

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

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        if(!ObjectUtils.isEmpty(newEntity.auditEntityIdentifier())){
            entityManager.detach(newEntity);
            return this.propertyTenantExtensionRepository.findById(newEntity.idOfEntity()).orElse(null);
        }
        return null;
    }
}
