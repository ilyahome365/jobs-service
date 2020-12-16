package com.home365.jobservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Immutable
public class Recurring {
    @Id
    private String id;
    private String pmAccountId;
    private String receiveAccountId;
    private String chargeAccountId;
    private String propertyId;
    private double amount;
    private String billType;
    private Timestamp createdOn;
    private Date dueDate;
    private String referenceNumber;
    private String memo;
    private String accountingTypeId;
    private String categoryId;
    private String statementType;
    private String checkMemo;
    private String fileUrl;
    private Double amountBeforeDiscount;
    private Date startDate;
    private Date endDate;
    private Double version;
    private String type;
    private String chargedBy;
    private Boolean active;
    private String leaseId;
    private int numOfInstallments;
    private int remainInstallments;
}
