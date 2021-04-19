package com.home365.jobservice.entities;


import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.model.enums.CreditType;
import com.home365.jobservice.service.AuditInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


@Entity
@Table(name = "Transactions")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transactions implements Serializable , IAuditableEntity {

    @Id
    @AuditInfo(ignore = true)
    private String transactionId;
    @AuditInfo(ignore = true)
    private String pmAccountId;
    @AuditInfo(ignore = true)
    private String receiveAccountId;
    @AuditInfo(ignore = true)
    private String chargeAccountId;
    @AuditInfo(ignore = true)
    private String propertyId;
    @AuditInfo(ignore = true)
    private String paymentId;
    private Long amount;
    @AuditInfo(ignore = true)
    private long amountBeforeDiscount;
    private String status;
    private String billType;
    private String isDeductible;
    private String isRecurring;
    @Column(updatable = false, insertable = false)
    private Timestamp createdOn;
    @Column(updatable = false, insertable = false)
    private Timestamp date;
    private Timestamp dueDate;
    @AuditInfo(viewName = "Reference number")
    private String referenceNumber;
    private String memo;
    @AuditInfo(ignore = true)
    private String accountingTypeId;
    @AuditInfo(ignore = true)
    private String categoryId;
    @AuditInfo(ignore = true)
    private String referenceTransactionId;
    @AuditInfo(ignore = true)
    private String incidentAccountId;
    private String accountingName;
    @AuditInfo(viewName = "Category")
    private String categoryName;
    private String statementType;
    private String checkMemo;
    @Column(updatable = false, insertable = false)
    private String transactionNumber;
    private String ownerDrawStatus;
    private String fileUrl;
    private String discountDescription;
    private String chargedBy;
    private String payBy;
    private String transactionType;
    @AuditInfo(ignore = true)
    private String recurringTemplateId;
    @Column(name = "CreditTransactionType")
    @Enumerated(value = EnumType.STRING)
    private CreditType creditTransactionType;

    @Column(name = "Version")
    private Long version;

    @Override
    public EntityType auditEntityType() {
        return EntityType.Transaction;
    }

    @Override
    public String auditEntityIdentifier() {
        if(this.getTransactionNumber() != null){
            return this.getTransactionNumber().toString();
        }
        return this.getTransactionId();
    }

    @Override
    public String idOfEntity() {
        return this.getTransactionId();
    }

    @Override
    public String auditMessage() {
        return this.getMemo();
    }

    public Integer getTransactionNumberAsInteger() {
        return Integer.parseInt(this.transactionNumber);
    }
}
