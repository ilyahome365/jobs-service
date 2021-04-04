package com.home365.jobservice.entities;

import com.home365.jobservice.entities.enums.CashPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CASH_PAYMENT_TRACKING")
@Builder
public class CashPaymentTracking {
    @Id
    String id;
    @Column(name = "related_transactions")
    String relatedTransactions;
    @Column
    @Enumerated(value = EnumType.STRING)
    CashPaymentStatus status;
    @Column(name = "paysafe_id")
    String paysafeId;
    @Column(name = "paysafe_response")
    String paysafeResponse;
    @Column(name = "tenant_id")
    String tenantId;
    @Column
    LocalDateTime created;
    @Column
    LocalDateTime updated;
    @Column(name = "is_sdd_payment")
    Boolean sddPayment;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
