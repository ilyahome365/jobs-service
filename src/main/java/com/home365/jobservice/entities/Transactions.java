package com.home365.jobservice.entities;


import com.home365.jobservice.entities.enums.OwnerDrawStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.home365.jobservice.config.Constants.*;


@Entity
@Table(name = "Transactions")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transactions implements Serializable {

    public static Set<String> expensesStatusSet = new HashSet<String>() {{
        add("Paid");
        add("paid");
    }};

    public static Set<String> approvedStatusSet = new HashSet<String>() {{
        add("readyForPayment");
        add("viliability");
        add("paymentFailed");
        add("pendingContribution");
    }};

    @Id
    private String transactionId = UUID.randomUUID().toString();
    private String pmAccountId;
    private String receiveAccountId;
    private String chargeAccountId;
    private String propertyId;
    private String paymentId;
    private long amount;
    private long amountBeforeDiscount;
    private String status;
    private String billType;
    private String isDeductible;
    private String isRecurring;
    @Column(updatable = false, insertable = false)
    private java.sql.Timestamp createdOn;
    @Column(updatable = false, insertable = false)
    private java.sql.Timestamp date;
    private java.sql.Timestamp dueDate;
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

    public String getTransactionType() {
        if (expensesStatusSet.contains(status)) {
            return EXPENSES;
        } else if (approvedStatusSet.contains(status)) {
            return APPROVED_BILLS;
        } else {
            return INCOMES;
        }
    }
//    @Column(insertable = false, updatable = false)
//    private Double projected_balance;

    @Override
    public String toString() {
        return "Transactions{" +
                "transactionId='" + transactionId + '\'' +
                '}';
    }
}
