package com.home365.jobservice.service;


import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;

import java.sql.Timestamp;

public interface PaymentsService extends  FindByIdAudit {
    Payments createAndSavePayments(double amount,
                                   Timestamp currentTimeAndDate,
                                   PaymentStatus status,
                                   String paymentReference,
                                   String transferId,
                                   String failedReason,
                                   String accountId,
                                   String pmAccountId,
                                   PaymentMethod paymentMethod);
}
