package com.home365.jobservice.service.impl;


import com.home365.jobservice.entities.IAuditableEntity;
import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.model.AuditEvent;
import com.home365.jobservice.repository.AuditEventRepository;
import com.home365.jobservice.service.AuditEventService;
import com.home365.jobservice.service.FindByIdAudit;
import com.home365.jobservice.service.RecurringService;
import com.home365.jobservice.service.TransactionsService;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final FindByIdAudit transactionService;
    private final FindByIdAudit recurringService;

    public AuditEventServiceImpl(AuditEventRepository auditEventRepository, TransactionsService transactionService, RecurringService recurringService) {
        this.auditEventRepository = auditEventRepository;
        this.transactionService = transactionService;
        this.recurringService = recurringService;
    }

    @Override
    public void audit(String userId, IAuditableEntity auditableEntity, boolean before) {
        try {
            if(before && getService(auditableEntity.auditEntityType()) != null){
                FindByIdAudit service = getService(auditableEntity.auditEntityType());
                IAuditableEntity auditableEntity1 = service.findByIdAudit(auditableEntity);
                if(auditableEntity1 == null){
                   audit(userId,auditableEntity,"Didnt found before");
                }else{
                    String comment = getDiffs(auditableEntity, auditableEntity1);
                    audit(userId, auditableEntity1,comment);
                }
            }else{
                audit(userId, auditableEntity,"After save");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return;
        }
    }

    private String getDiffs(IAuditableEntity auditableEntity, IAuditableEntity auditableEntity1) {
        DiffNode diffNode = ObjectDifferBuilder.buildDefault().compare(auditableEntity1, auditableEntity);
        StringBuilder  diffs = new StringBuilder();
        if (diffNode.hasChanges()) {
            diffNode.visit((node, visit) -> {
                if (!node.hasChildren()) { // Only print if the property has no child
                    final Object oldValue = node.canonicalGet(auditableEntity1);
                    final Object newValue = node.canonicalGet(auditableEntity);
                    diffs.append(" ").append( node.getPropertyName()).append(" changed from ")
                            .append(oldValue).append(" to ").append( newValue).append(" , ");
                }
            });
        } else {
            diffs.append("No differences");
        }
        return  diffs.toString();
    }


    private void audit(String userId, IAuditableEntity auditableEntity,String comment) {
        AuditEvent auditEvent = mapToAuditEvent(userId, auditableEntity,comment);
        auditEventRepository.save(auditEvent);
    }

    private AuditEvent mapToAuditEvent(String userId, IAuditableEntity auditableEntity, String comment) {
        AuditEvent auditEvent = AuditEvent.builder()
                .entityType(auditableEntity.auditEntityType())
                .entityIdentifier(auditableEntity.auditEntityIdentifier())
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .comment(comment)
                .message(auditableEntity.auditMessage())
                .build();
        return auditEvent;
    }

    private FindByIdAudit getService(EntityType entityType){
        if(entityType.equals(EntityType.Transaction)){
            return this.transactionService;
        }
        if(entityType.equals(EntityType.Recurring)){
            return this.recurringService;
        }
        return null;
    }
}
