package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.repository.TransactionsRepository;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;

    public TransactionsServiceImpl(TransactionsRepository transactionsRepository) {
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public List<Transactions> saveAllTransactions(List<Transactions> transactions) {
        log.info("enter to save All Transactions {} ", transactions);
        return transactionsRepository.saveAll(transactions);
    }

    @Override
    public List<Transactions> getTransactionsWithProjectedBalance(String cycleDate) {
        log.info("Enter to get all transactions for cycle : {}", cycleDate);
        return transactionsRepository.getTransactionsWithProjectedBalance(cycleDate);
    }

    @Override
    public List<Transactions> findAllByBillTypeAndStatusAndDueDateBefore(List<String> billTypes, List<String> status, java.sql.Timestamp time) {
        return transactionsRepository.findAllByBillTypeAndStatusAndDueDateBefore(billTypes, status, time);
    }

    @Override
    public List<Transactions> findByRecurringTemplateIdAndDueDateAfter(String recurringTemplateId, Date date) {
        return transactionsRepository.findByRecurringTemplateIdAndDueDateAfter(recurringTemplateId, date);
    }
}
