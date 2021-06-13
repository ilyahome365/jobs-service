package com.home365.jobservice.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.Transactions;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.home365.jobservice.utils.BusinessActionRequest.getBusinessAction;

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
    private final FindByIdAudit propertyTenantExtensionService;
    public static final String DELETED = "Deleted";
    public static final String NO_DIFFERENCES = "No differences";
    public static final String WAS_CREATED = " was created";
    DateTimeFormatter dateTimeFormatter;

    public AuditEventServiceImpl(AuditEventRepository auditEventRepository, TransactionsService transactionService, RecurringService recurringService,
                                 PaymentsService paymentsService,
                                 PropertyTenantExtensionServiceImpl propertyTenantExtensionService) {
        dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        this.auditEventRepository = auditEventRepository;
        this.transactionService = transactionService;
        this.recurringService = recurringService;
        this.paymentService = paymentsService;
        this.propertyTenantExtensionService = propertyTenantExtensionService;
    }

    @Override
    public void audit(String userId, List<IAuditableEntity> newEntities){
        if(!CollectionUtils.isEmpty(newEntities)){
            List<IAuditableEntity> validEnt = newEntities.stream().filter(ne -> !ObjectUtils.isEmpty(ne.idOfEntity())).collect(Collectors.toList());
            List<IAuditableEntity> notValid = newEntities.stream().filter(ne -> ObjectUtils.isEmpty(ne.idOfEntity())).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(validEnt)){
                FindByIdAudit findByIdAudit = getService(validEnt.get(0).auditEntityType());
                if(findByIdAudit != null){
                    List<IAuditableEntity> oldEntities = findByIdAudit.findByList(validEnt);
                    if(!CollectionUtils.isEmpty(oldEntities)){
                        Map<String, IAuditableEntity> newEnt = validEnt.stream().collect(Collectors.toMap(IAuditableEntity::idOfEntity, Function.identity()));
                        Map<String, IAuditableEntity> oldEnt = oldEntities.stream().collect(Collectors.toMap(IAuditableEntity::idOfEntity, Function.identity()));
                        newEnt.forEach((key,value) -> {
                            if(oldEnt.containsKey(key)){
                                IAuditableEntity oldEntity = oldEnt.get(key);
                                CommentHolder commentHolder = getDiffs(value, oldEntity, true);
                                persist(userId, value, commentHolder);
                            }else{
                                CommentHolder commentHolder = getDiffs(value, null, false);
                                persist(userId, value, commentHolder);
                            }
                        });
                    }else{
                        validEnt.forEach(nEnt -> audit(userId,nEnt));
                    }
                }else{
                    validEnt.forEach(nEnt -> audit(userId,nEnt));
                }

            }
        }

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
        List<String> comments = new LinkedList<>();
        commentHolder.setComments(comments);
        if (oldEntity != null) {
            compareEntities(newEntity, oldEntity, comments);
        } else {
            if (!serviceFound) {
                comments.add("Problem with extracting transaction");
            } else {
                String businessAction = getBusinessAction();
                String businessNameForEntity = getBusinessNameForEntity(newEntity);
                if (!ObjectUtils.isEmpty(businessAction)) {
                    comments.add(businessNameForEntity + "  due to " + businessAction);
                } else {
                    comments.add(businessNameForEntity);
                }
            }
        }
        return commentHolder;
    }
    private void compareEntities(IAuditableEntity newEntity, IAuditableEntity oldEntity, List<String> comments) {
        DiffNode diffNode = ObjectDifferBuilder.startBuilding().comparison()
                .ofType(LocalDateTime.class).toUseEqualsMethod().and().comparison().ofType(LocalDate.class).toUseEqualsMethod().and().build().compare(oldEntity, newEntity);
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
                    String action = null;
                    String removedAction = null;
                    if (auditInfo != null) {
                        ignoreField = auditInfo.ignore();
                        fieldName = auditInfo.viewName().equalsIgnoreCase("") ? node.getPropertyName() : auditInfo.viewName();
                    }
                    if (ObjectUtils.isEmpty(newValue) && ObjectUtils.isEmpty(oldEntity)) {
                        ignoreField = true;
                    }
                    if (isDate(oldValue)) {
                        boolean isDateChanged = isDateChanged(newValue, oldValue);
                        if (!isDateChanged) {
                            ignoreField = true;
                        }
                    }
                    diffs.append(" ");
                    FindByIdAudit service = getService(newEntity.auditEntityType());
                    if (service != null) {
                        Optional<Object> optionalNewValue = service.handleSpecialProperty(node.getPropertyName(), newEntity, newValue);
                        Optional<Object> optionalOldValue = service.handleSpecialProperty(node.getPropertyName(), oldEntity, oldValue);
                        if (optionalNewValue.isPresent()) {
                            newValue = optionalNewValue.get();
                        }
                        if (optionalOldValue.isPresent()) {
                            oldValue = optionalOldValue.get();
                        }
                    }
                    String businessAction = getBusinessAction();
                    if (ObjectUtils.isEmpty(oldValue) &&  ObjectUtils.isEmpty((newValue))) {
                        ignoreField = true;
                    }
                    if (!ignoreField && oldValue instanceof String && newValue instanceof String) {
                        if (((String) oldValue).toLowerCase().equalsIgnoreCase(((String) newValue).toLowerCase())) {
                            ignoreField = true;
                        }
                    }
                    if (ignoreField) {
                        diffs.append("@ ");
                    }
                    if (!ObjectUtils.isEmpty(removedAction) && ObjectUtils.isEmpty(newValue)) {
                        diffs.append(removedAction);
                    } else {
                        if (!ObjectUtils.isEmpty(action)) {
                            diffs.append(action);
                        } else {
                            diffs.append(splitCameCase(fieldName))
                                    .append(" was ")
                                    .append(getValue(oldValue, true))
                                    .append(" to ")
                                    .append(getValue(newValue, false)).append(" ");
                        }
                    }
                    if (!ObjectUtils.isEmpty(businessAction)) {
                        diffs.append(" due to ").append(businessAction);
                    }
                    if (service != null) {
                        Optional<Object> auditEnding = service.handledEndingOfAudit(newEntity);
                        auditEnding.ifPresent(diffs::append);
                    }
                    comments.add(diffs.toString());
                }
            });
        } else {
            diffs.append(NO_DIFFERENCES);
            comments.add(diffs.toString());
        }
    }

    private String splitCameCase(String type) {
        if (!ObjectUtils.isEmpty(type)) {
            String[] strings = org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(type);
            List<String> upperCase = new LinkedList<>();
            for (String string : strings) {
                upperCase.add(org.apache.commons.lang3.StringUtils.capitalize(string));
            }
            return String.join(" ", upperCase);
        }
        return type;
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


    private Object getValue(Object value, boolean addFrom) {
        String edited = " edited from ";
        Object result = value;
        if (value == null) {
            if (addFrom) {
                return " set ";
            } else {
                return " empty ";
            }
        }
        if (value instanceof Boolean) {
            Boolean val = ((Boolean) value);
            if (val) {
                result = "yes";
            } else {
                result = "no";
            }
        }
        if(value instanceof LocalDateTime){
            LocalDate result1 = ((LocalDateTime) value).toLocalDate();
            result = dateTimeFormatter.format(result1);
        }
        if(value instanceof Timestamp){
            result =   dateTimeFormatter.format(((Timestamp)value).toLocalDateTime().toLocalDate());
        }
        if(value instanceof LocalDate){
            result =  dateTimeFormatter.format(((LocalDate)value));
        }
        if (isNumber(value)) {
            result = handleAmount(value);
        }

        if (value instanceof String) {
            if (org.apache.commons.lang3.StringUtils.isAlphaSpace((String) value)) {
                result = splitCameCase((String) value);
            }
        }
        if (!addFrom) {
            return result;
        } else {
            return edited + result;
        }
    }

    private boolean isNumber(Object value) {
        return value instanceof Number;
    }


    private Object handleAmount(Object amount) {
        DecimalFormat format = new DecimalFormat("0.##");
        if (!ObjectUtils.isEmpty(amount)) {
            if (amount instanceof Long) {
                return  "$" +  format.format((Long) amount / 100D);
            } else {
                if (amount instanceof String) {
                    try {
                        return   "$"  + format.format(Long.parseLong(String.valueOf(amount)) / 100D) ;
                    } catch (Exception exception) {
                        log.warn("Error casting amount : {}", exception.getMessage());
                        return amount;
                    }
                }
            }
        }
        return amount;
    }

    @Transactional
    public void persist(String userId, IAuditableEntity auditableEntity, CommentHolder commentHolder) {
        AuditEvent auditEvent = mapToAuditEvent(userId, auditableEntity, commentHolder.toString());
        auditEventRepository.save(auditEvent);
    }

    private AuditEvent mapToAuditEvent(String userId, IAuditableEntity auditableEntity, String comment) {
        AuditEvent auditEvent = AuditEvent.builder()
                .entityType(auditableEntity.auditEntityType())
                .entityIdentifier(auditableEntity.auditEntityIdentifier())
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .createdOn(LocalDateTime.now(ZoneOffset.UTC))
                .updatedOn(LocalDateTime.now(ZoneOffset.UTC))
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
        if(entityType.equals(EntityType.PAYMENT)){
            return this.paymentService;
        }
        if(entityType.equals(EntityType.Property)){
            return this.propertyTenantExtensionService;
        }
        return null;
    }


    private String getBusinessNameForEntity(IAuditableEntity entity) {
        if (EntityType.Transaction.equals(entity.auditEntityType())) {
            Transactions entity1 = (Transactions) entity;
            return splitCameCase(entity1.getBillType()) + WAS_CREATED;
        }
        if(EntityType.Recurring.equals(entity.auditEntityType())){
            return " recurring charge"  + WAS_CREATED;
        }
        if (EntityType.PAYMENT.equals(entity.auditEntityType())) {
           return " payment " + WAS_CREATED;
        }
        return CREATED;
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
