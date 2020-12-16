package com.home365.jobservice.service;

import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.Transactions;

import java.util.List;

public interface TransactionsService {
    List<Transactions> saveAllTransactions(List<Transactions> transactions);

    List<TransactionsWithProjectedBalance> getTransactionsWithProjectedBalance(String cycleDate);

    List<Transactions> findAllByBillTypeAndStatus(List<String> categoryNames, List<String> status);

    List<Transactions> findByRecurringTemplateId(String recurringTemplateId);

    ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();
    void saveTransaction(Transactions transactions);

    void saveAllTransactionsWithProjectedBalance(List<TransactionsWithProjectedBalance >transactionsWithProjectedBalances);
}
