package com.home365.jobservice.service;

import com.home365.jobservice.entities.Transactions;

import java.util.List;

public interface TransactionsService {
    List<Transactions> saveAllTransactions(List<Transactions> transactions);
    List<Transactions> getTransactionsWithProjectedBalance(String cycleDate);
}
