package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.projection.IAuditableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

public abstract  class FindByAuditAbstract {

    private final JpaRepository<IAuditableEntity,String> repository;

    @PersistenceContext
    private EntityManager entityManager;

    public FindByAuditAbstract(JpaRepository<IAuditableEntity, String> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true,propagation = Propagation.REQUIRES_NEW)
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

}
