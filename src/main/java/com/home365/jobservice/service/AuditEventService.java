package com.home365.jobservice.service;


import com.home365.jobservice.entities.projection.IAuditableEntity;

public interface AuditEventService {
    void audit(String userId, IAuditableEntity transaction);
}
