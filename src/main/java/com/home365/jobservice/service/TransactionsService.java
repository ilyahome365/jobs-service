package com.home365.jobservice.service;

import com.home365.jobservice.entities.Transactions;

import java.util.Date;
import java.util.List;

public interface TransactionsService {
    List<Transactions> saveAllTransactions(List<Transactions> transactions);
    List<Transactions> getTransactionsWithProjectedBalance(String cycleDate);
    List<Transactions> findAllByBillTypeAndStatusAndDueDateBefore(List<String> billTypes, List<String> status, Date time);
    List<Transactions> findByRecurringTemplateId(String recurringTemplateId);
}
