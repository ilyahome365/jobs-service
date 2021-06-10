package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.repository.PaymentsRepo;
import com.home365.jobservice.service.FindByIdAudit;
import com.home365.jobservice.service.PaymentsService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.List;

@Service
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentsRepo paymentsRepo;

    private final FindByIdAudit findByIdAudit;

    public PaymentsServiceImpl(PaymentsRepo paymentsRepo) {
        super();
        this.findByIdAudit = new FindByAuditImpl(paymentsRepo);
        this.paymentsRepo = paymentsRepo;
    }

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        return findByIdAudit.findByIdAudit(newEntity);
    }

    @Override
    public List<IAuditableEntity> findByList(List<IAuditableEntity> entityList) {
       return findByIdAudit.findByList(entityList);
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
