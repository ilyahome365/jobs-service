package com.home365.jobservice.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.entities.projection.IAuditableEntity;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {
    public static final String AMOUNT = "amount";
    public static final String PROBLEM_WITH_EXTRACTING_TRANSACTION = "Problem with extracting transaction";
    public static final String CREATED = "Created";
    public static final String AMOUNT_NOT_FOUND = "Amount not found";
    private final AuditEventRepository auditEventRepository;
    private final FindByIdAudit transactionService;
    private final FindByIdAudit recurringService;
    private final FindByIdAudit paymentService;
    public static final String DELETED = "Deleted";
    public static final String NO_DIFFERENCES = "No differences";

    public AuditEventServiceImpl(AuditEventRepository auditEventRepository, TransactionsService transactionService, RecurringService recurringService,
                                 PaymentsService paymentsService) {
        this.auditEventRepository = auditEventRepository;
        this.transactionService = transactionService;
        this.recurringService = recurringService;
        this.paymentService = paymentsService;
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
                comments.add(PROBLEM_WITH_EXTRACTING_TRANSACTION);
            } else {
                comments.add(CREATED);
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
                    if(ObjectUtils.isEmpty(newValue) && ObjectUtils.isEmpty(oldEntity)){
                        ignoreField = true;
                    }
                    if(isDate(oldValue)){
                        boolean isDateChanged =  isDateChanged(newValue,oldValue);
                        if(!isDateChanged){
                            ignoreField = true;
                        }
                    }
                    if (node.getPropertyName().equalsIgnoreCase(AMOUNT) || node.getPropertyName().equalsIgnoreCase("amountBeforeDiscount")) {
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


    private boolean isDate(Object dateNew){
        return dateNew instanceof LocalDate || dateNew instanceof Timestamp || dateNew instanceof LocalDateTime;
    }

    private boolean isDateChanged(Object newValue, Object oldValue) {
        if(newValue != null && oldValue != null){
            if(newValue  instanceof Timestamp && oldValue instanceof Timestamp){
                return  !((Timestamp)newValue).toLocalDateTime().toLocalDate().atStartOfDay().equals(((Timestamp) oldValue).toLocalDateTime().toLocalDate().atStartOfDay());
            }
            if(newValue instanceof LocalDate && oldValue instanceof LocalDate){
                return  !((LocalDate)newValue).atStartOfDay().equals(((LocalDate)oldValue).atStartOfDay());
            }
            if(newValue instanceof LocalDateTime && oldValue instanceof LocalDateTime){
                return  !((LocalDateTime)newValue).toLocalDate().atStartOfDay().equals(((LocalDateTime)oldValue).toLocalDate().atStartOfDay());
            }
        }
        return true;
    }

    private AuditInfo getAuditInfo(DiffNode node) {
        return Optional.ofNullable(node.getFieldAnnotations())
                .orElse(Collections.emptySet())
                .stream()
                .filter(anno -> anno.annotationType().equals(AuditInfo.class))
                .map(AuditInfo.class::cast)
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
            DecimalFormat format = new DecimalFormat("0.##");
            Field amount = newEntity.getClass().getDeclaredField(AMOUNT);
            amount.setAccessible(true);
            Object am = amount.get(newEntity);
            if (am instanceof Long) {
                return  format.format ((Long) am / 100d);
            }
            return am.toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error(e.getMessage());
            return AMOUNT_NOT_FOUND;
        }
    }


    private Object handleAmount(Object amount) {
        DecimalFormat format = new DecimalFormat("0.##");
        if (!ObjectUtils.isEmpty(amount)) {
            if (amount instanceof Long) {
                return format.format((Long) amount / 100D) + "$";
            } else {
                if (amount instanceof String) {
                    try {
                        return format.format(Long.parseLong(String.valueOf(amount)) / 100D) +  "$";
                    } catch (Exception exception) {
                        log.warn("Error casting amount : {}", exception.getMessage());
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
        return AuditEvent.builder()
                .entityType(auditableEntity.auditEntityType())
                .entityIdentifier(auditableEntity.auditEntityIdentifier())
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .comment(comment)
                .message(auditableEntity.auditMessage())
                .build();
    }

    private FindByIdAudit getService(EntityType entityType){
        if(entityType.equals(EntityType.Transaction)){
            return this.transactionService;
        }
        if(entityType.equals(EntityType.Recurring)){
            return this.recurringService;
        }
        if(entityType.equals(EntityType.PAYMENT)){
            return this.paymentService;
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
                return Constants.EMPTY_STRING;
            }
        }
    }
}
