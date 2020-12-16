package com.home365.jobservice.entities;


import com.home365.jobservice.entities.enums.OwnerDrawStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;


@Entity
@Table(name = "Transactions")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transactions implements Serializable {

    @Id
    private String transactionId;
    private String pmAccountId;
    private String receiveAccountId;
    private String chargeAccountId;
    private String propertyId;
    private String paymentId;
    private Long amount;
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
    private String referenceNumber;
    private String memo;
    private String accountingTypeId;
    private String categoryId;
    private String referenceTransactionId;
    private String incidentAccountId;
    private String accountingName;
    private String categoryName;
    private String statementType;
    private String checkMemo;
    @Column(updatable = false, insertable = false)
    private String transactionNumber;
    @Enumerated(value = EnumType.STRING)
    private OwnerDrawStatus ownerDrawStatus;
    private String fileUrl;
    private String discountDescription;
    private String chargedBy;
    private String payBy;
    private String transactionType;
    private String recurringTemplateId;

    @Version
    @Column(name = "Version")
    private Long version;



}
