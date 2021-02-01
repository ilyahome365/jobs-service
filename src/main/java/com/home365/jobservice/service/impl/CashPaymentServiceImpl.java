package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.CashPaymentTracking;
import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.enums.CashPaymentStatus;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;
import com.home365.jobservice.repository.CashPaymentTrackingRepository;
import com.home365.jobservice.service.CashPaymentService;
import com.home365.jobservice.service.PaymentsService;
import com.home365.jobservice.service.TransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class CashPaymentServiceImpl implements CashPaymentService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    CashPaymentTrackingRepository cashPaymentTrackingRepository;

    @Autowired
    TransactionsService transactionService;

    @Autowired
    PaymentsService paymentsService;

    public void handleCashPaymentWebhookResponse(Object request) {
        Map<String, Object> requestMap = (Map<String, Object>) request;
        String eventType = (String) requestMap.get("eventType");
        String mtid = (String) ((Map<String, Object>) requestMap.get("data")).get("mtid");

        Optional<CashPaymentTracking> cashPaymentTrackingOpt = cashPaymentTrackingRepository.findByPaysafeId(mtid);

        if (cashPaymentTrackingOpt.isPresent()) {
            CashPaymentTracking cashPaymentTracking = cashPaymentTrackingOpt.get();
            if (!CashPaymentStatus.PAYMENT_EXPIRED.name().equalsIgnoreCase(eventType)) {
                List<String> transactions = Arrays.asList(cashPaymentTracking.getRelatedTransactions().split(",", -1));
                transactions.forEach(transaction -> {
                    Optional<Transactions> transactionsOptional = null;

                    transactionsOptional = transactionService.findById(transaction);

                    if (transactionsOptional.isPresent()) {
                        Transactions transactionObject = transactionsOptional.get();

                        Payments paymentObj = paymentsService.createAndSavePayments(
                                transactionObject.getAmount(),
                                new Timestamp(new Date().getTime()),
                                PaymentStatus.success,
                                mtid,
                                null,
                                null,
                                transactionObject.getReceiveAccountId(),
                                transactionObject.getPmAccountId(),
                                PaymentMethod.cash
                        );

                        transactionObject.setStatus("paid");
                        transactionObject.setPaymentId(paymentObj.getPaymentId());
                        transactionService.save(transactionObject);
                    }
                });
            }
            cashPaymentTracking.setStatus(CashPaymentStatus.valueOf(eventType));
            cashPaymentTrackingRepository.save(cashPaymentTracking);
        }
    }
}
