package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.service.FindByIdAudit;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FindByAuditImpl implements FindByIdAudit {

    private JpaRepository<IAuditableEntity,String> repository;

    @PersistenceContext
    private EntityManager entityManager;


    public  IAuditableEntity findByIdAudit(IAuditableEntity newEntity){
        IAuditableEntity auditableEntity = null;
        if(!ObjectUtils.isEmpty(newEntity.idOfEntity())){
            entityManager.detach(newEntity);
            auditableEntity  =  this.repository.findById(newEntity.idOfEntity()).orElse(null);
        }
        return  auditableEntity;
    }

    @Override
    public void setRepository(JpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public  IAuditableEntity findByIdAudit(IAuditableEntity newEntity,JpaRepository repository){
        if(!ObjectUtils.isEmpty(newEntity.idOfEntity())){
            entityManager.detach(newEntity);
            return (IAuditableEntity) repository.findById(newEntity.idOfEntity()).orElse(null);
        }
        return null;
    }

    public List<IAuditableEntity> findByList(List<IAuditableEntity> entityList){
        List<IAuditableEntity> result = null;
        if(!CollectionUtils.isEmpty(entityList)){
            List<String> ids = entityList.stream().filter(ent -> !ObjectUtils.isEmpty(ent.idOfEntity()))
                    .map(IAuditableEntity::idOfEntity).collect(Collectors.toList());
            List<IAuditableEntity> collect = entityList.stream().filter(ent -> !ObjectUtils.isEmpty(ent.idOfEntity()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(ids)){
                collect.forEach(cl -> entityManager.detach(cl));
                result =  this.repository.findAllById(ids);
            }
        }
        return result;
    }


    public Optional<Object> handleSpecialProperty(String propertyName, IAuditableEntity iAuditableEntity, Object value){
        return Optional.empty();
    }


}
