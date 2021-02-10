package com.home365.jobservice.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.IAuditableEntity;
import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.model.AuditEvent;
import com.home365.jobservice.repository.AuditEventRepository;
import com.home365.jobservice.service.*;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final FindByIdAudit transactionService;
    private final FindByIdAudit recurringService;
    public static final String DELETED = "Deleted";
    public static final String NO_DIFFERENCES = "No differences";

    public AuditEventServiceImpl(AuditEventRepository auditEventRepository, TransactionsService transactionService, RecurringService recurringService) {
        this.auditEventRepository = auditEventRepository;
        this.transactionService = transactionService;
        this.recurringService = recurringService;
    }

    @Override
    public void audit(String userId, IAuditableEntity newEntity) {
        try {
            FindByIdAudit service = getService(newEntity.auditEntityType());
            if (service != null) {
                IAuditableEntity oldEntity = service.findByIdAudit(newEntity);
                CommentHolder commentHolder = getDiffs(newEntity, oldEntity, true);
                persist(userId, newEntity, commentHolder);
            } else {
                CommentHolder commentHolder = getDiffs(newEntity, null, false);
                persist(userId, newEntity, commentHolder);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return;
        }
    }


    private CommentHolder getDiffs(IAuditableEntity newEntity, IAuditableEntity oldEntity, boolean serviceFound) {
        CommentHolder commentHolder = new CommentHolder();
        commentHolder.setAmount(getNewAmount(newEntity));
        List<String> comments = new LinkedList<>();
        commentHolder.setComments(comments);
        if (oldEntity != null) {
            compareEntities(newEntity, oldEntity, comments);
        } else {
            if (!serviceFound) {
                comments.add("Problem with extracting transaction");
            } else {
                comments.add("Created");
            }
        }
        return commentHolder;
    }

    private void compareEntities(IAuditableEntity newEntity, IAuditableEntity oldEntity, List<String> comments) {
        DiffNode diffNode = ObjectDifferBuilder.buildDefault().compare(oldEntity, newEntity);
        StringBuilder diffs = new StringBuilder();
        if (diffNode.hasChanges()) {
            diffNode.visit((node, visit) -> {
                if (!node.hasChildren()) {// Only print if the property has no child
                    diffs.setLength(0);
                    Object oldValue = node.canonicalGet(oldEntity);
                    Object newValue = node.canonicalGet(newEntity);
                    AuditInfo auditInfo = getAuditInfo(node);
                    boolean ignoreField = false;
                    String fieldName = node.getPropertyName();
                    if(auditInfo != null){
                        ignoreField = auditInfo.ignore();
                        fieldName = auditInfo.viewName().equalsIgnoreCase("") ? node.getPropertyName() : auditInfo.viewName();
                    }
                    if (node.getPropertyName().equalsIgnoreCase("amount") || node.getPropertyName().equalsIgnoreCase("amountBeforeDiscount")) {
                        oldValue = handleAmount(oldValue);
                        newValue = handleAmount(newValue);
                    }
                    diffs.append(" ");
                    if (ignoreField) {
                        diffs.append("@ ");
                    }
                    diffs.append(fieldName).append(" changed from ");
                    diffs.append(getValue(oldValue) ).append(" to ").append(getValue(newValue)).append(" ");
                    comments.add(diffs.toString());
                }
            });
        } else {
            diffs.append(NO_DIFFERENCES);
            comments.add(diffs.toString());
        }
    }

    private AuditInfo getAuditInfo(DiffNode node) {
        return Optional.ofNullable(node.getFieldAnnotations())
                .orElse(Collections.emptySet())
                .stream()
                .filter(anno -> anno.annotationType().equals(AuditInfo.class))
                .map(annotation -> (AuditInfo)annotation)
                .findFirst().orElse(null);
    }


    private Object getValue(Object value){
        if(value == null){
            return  "empty";
        }
        if(value instanceof LocalDateTime){
            return  ((LocalDateTime) value).toLocalDate();
        }
        if(value instanceof Timestamp){
            return  ((Timestamp)value).toLocalDateTime().toLocalDate();
        }
        return value;
    }

    private String getNewAmount(IAuditableEntity newEntity) {
        try {
            Field amount = newEntity.getClass().getDeclaredField("amount");
            amount.setAccessible(true);
            Object am = amount.get(newEntity);
            if (am instanceof Long) {
                long sum = (Long) am / 100;
                return Long.toString(sum);
            }
            return am.toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error(e.getMessage());
            return "Amount nof found";
        }
    }


    private Object handleAmount(Object amount) {
        if (!ObjectUtils.isEmpty(amount)) {
            if (amount instanceof Long) {
                return (Long) amount / 100;
            } else {
                if (amount instanceof String) {
                    try {
                        long newAm = Long.parseLong(String.valueOf(amount)) / 100;
                        return Long.toString(newAm);
                    } catch (Exception exception) {
                        log.error("Error casting amount : {}", exception.getMessage());
                        return amount;
                    }
                }
            }
        }
        return amount;
    }

    private void persist(String userId, IAuditableEntity auditableEntity, CommentHolder commentHolder) {
        AuditEvent auditEvent = mapToAuditEvent(userId, auditableEntity, commentHolder.toString());
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


    @Data
    public static class CommentHolder {
        String amount;
        List<String> comments;
        private static ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                return null;
            }
        }
    }
}
