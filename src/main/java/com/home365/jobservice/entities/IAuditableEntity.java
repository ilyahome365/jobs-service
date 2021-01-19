package com.home365.jobservice.entities;


import com.home365.jobservice.entities.enums.EntityType;

public interface IAuditableEntity {


    EntityType auditEntityType();
    String auditEntityIdentifier();
    String auditMessage();
    String idOfEntity();
}
