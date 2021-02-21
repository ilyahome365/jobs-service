package com.home365.jobservice.entities;

import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payments implements IAuditableEntity {

    @Id
    @Column(name = "PaymentId")
    private String paymentId = UUID.randomUUID().toString();
    @Basic
    @Column(name = "PaymentMethod")
    private String paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String failedReason;
    private String paymentReference;
    private String transferReference;
    private long amount;
    private Timestamp date;
    @Column(name = "AccountId")
    private String accountId;
    @Column(name = "PmAccountId")
    private String pmAccountId;
    private String checkNumber;


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPmAccountId() {
        return pmAccountId;
    }

    public void setPmAccountId(String pmAccountId) {
        this.pmAccountId = pmAccountId;
    }

    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }


    @Basic
    @Column(name = "Status")
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @Basic
    @Column(name = "FailedReason")
    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    @Basic
    @Column(name = "PaymentReference")
    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @Basic
    @Column(name = "TransferReference")
    public String getTransferReference() {
        return transferReference;
    }

    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }

    @Basic
    @Column(name = "Amount")
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Basic
    @Column(name = "CheckNumber")
    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    @Basic
    @Column(name = "Date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payments payments = (Payments) o;

        if (amount != payments.amount) return false;
        if (!Objects.equals(paymentId, payments.paymentId)) return false;
        if (!Objects.equals(paymentMethod, payments.paymentMethod))
            return false;
        if (!Objects.equals(status, payments.status)) return false;
        if (!Objects.equals(failedReason, payments.failedReason))
            return false;
        if (!Objects.equals(paymentReference, payments.paymentReference))
            return false;
        if (!Objects.equals(transferReference, payments.transferReference))
            return false;
        return Objects.equals(date, payments.date);
    }

    @Override
    public int hashCode() {
        int result = paymentId != null ? paymentId.hashCode() : 0;
        result = 31 * result + (paymentMethod != null ? paymentMethod.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (failedReason != null ? failedReason.hashCode() : 0);
        result = 31 * result + (paymentReference != null ? paymentReference.hashCode() : 0);
        result = 31 * result + (transferReference != null ? transferReference.hashCode() : 0);
        result = 31 * result + (int) (amount ^ (amount >>> 32));
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public EntityType auditEntityType() {
        return EntityType.PAYMENT;
    }

    @Override
    public String auditEntityIdentifier() {
        return this.getPaymentId();
    }

    @Override
    public String auditMessage() {
        return this.getFailedReason();
    }

    @Override
    public String idOfEntity() {
        return this.getPaymentId();
    }
}
