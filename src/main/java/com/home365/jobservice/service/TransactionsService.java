package com.home365.jobservice.service;

import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.Transactions;

import java.util.Date;
import java.util.List;

public interface TransactionsService {
    List<Transactions> saveAllTransactions(List<Transactions> transactions);

    List<Transactions> getTransactionsWithProjectedBalance(String cycleDate);

    List<Transactions> findAllByBillTypeAndStatus(List<String> categoryNames, List<String> status);

    List<Transactions> findByRecurringTemplateId(String recurringTemplateId);

    ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();

    List<Transactions> findTenantRentTransactionsByPropertyId(String propertyId);

    void save(Transactions transaction);
}
