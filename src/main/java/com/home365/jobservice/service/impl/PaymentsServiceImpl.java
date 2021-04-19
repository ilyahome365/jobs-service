package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.repository.PaymentsRepo;
import com.home365.jobservice.service.PaymentsService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;

@Service
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentsRepo paymentsRepo;

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentsServiceImpl(PaymentsRepo paymentsRepo) {
        super();
        this.paymentsRepo = paymentsRepo;
    }

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        entityManager.detach(newEntity);
        return this.paymentsRepo.findById(newEntity.idOfEntity()).orElse(null);
    }

    @Override
    public Payments createAndSavePayments(double amount,
                                          Timestamp currentTimeAndDate,
                                          PaymentStatus status,
                                          String paymentReference,
                                          String transferId,
                                          String failedReason,
                                          String accountId,
                                          String pmAccountId,
                                          PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.transfer;
        }
        Payments payments = new Payments().toBuilder()
                .amount((long) amount)
                .accountId(accountId)
                .pmAccountId(pmAccountId)
                .date(currentTimeAndDate)
                .paymentReference(paymentReference)
                .transferReference(transferId)
                .status(status)
                .paymentMethod(paymentMethod.name())
                .failedReason(failedReason)
                .build();
        return paymentsRepo.save(payments);
    }
}
