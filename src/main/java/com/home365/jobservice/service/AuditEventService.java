package com.home365.jobservice.service;


import com.home365.jobservice.entities.projection.IAuditableEntity;

import java.util.List;

public interface AuditEventService {
    void audit(String userId, List<IAuditableEntity> newEntities);

    void audit(String userId, IAuditableEntity transaction);
}
