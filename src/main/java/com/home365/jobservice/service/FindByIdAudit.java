package com.home365.jobservice.service;


import com.home365.jobservice.entities.projection.IAuditableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FindByIdAudit {

    @Transactional
    IAuditableEntity findByIdAudit(IAuditableEntity newEntity);

    default Optional<Object> handleSpecialProperty(String propertyName, IAuditableEntity iAuditableEntity, Object value){
        return Optional.empty();
    }

    default Optional<Object> handledEndingOfAudit(IAuditableEntity iAuditableEntity){
        return Optional.empty();
    }

    List<IAuditableEntity> findByList(List<IAuditableEntity> entityList);

    void setRepository(JpaRepository repository);

}
