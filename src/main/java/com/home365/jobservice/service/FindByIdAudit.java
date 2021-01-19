package com.home365.jobservice.service;


import com.home365.jobservice.entities.IAuditableEntity;
import org.springframework.transaction.annotation.Transactional;

public interface FindByIdAudit {

    @Transactional
    IAuditableEntity findByIdAudit(IAuditableEntity newEntity);
}
