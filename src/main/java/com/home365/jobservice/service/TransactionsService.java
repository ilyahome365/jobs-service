package com.home365.jobservice.service;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.projection.IOwnerRentNotification;

import java.util.List;
import java.util.Optional;

public interface TransactionsService extends FindByIdAudit{
    List<Transactions> saveAllTransactions(List<Transactions> transactions);

    List<TransactionsWithProjectedBalance> getTransactionsWithProjectedBalance(String cycleDate, String locationId);

    List<Transactions> findAllByBillTypeAndStatus(List<String> categoryNames, List<String> status, String pmAccountId);

    List<Transactions> findByRecurringTemplateId(String recurringTemplateId);

    ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();
    void saveTransaction(Transactions transactions);

    void saveAllTransactionsWithProjectedBalance(List<TransactionsWithProjectedBalance >transactionsWithProjectedBalances);

    List<Transactions> findTenantRentTransactionsByPropertyId(String propertyId);

    Transactions save(Transactions transaction);

    Optional<Transactions> findById(String accountId);

    void saveTransactionsWithBalance(TransactionsWithProjectedBalance transactionsWithProjectedBalance);

    Optional<Transactions> findTransaction(String transactionId);

    void saveAll(List<Transactions> mgmtFeeTransactions);

    List<IOwnerRentNotification> getTransactionsForOwnerRent(String pmAccount , String firstDate ,String lastDate);




}
