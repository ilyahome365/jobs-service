package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.TransactionsLog;
import com.home365.jobservice.repository.TransactionLogRepository;
import com.home365.jobservice.service.TransactionsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionsLogServiceImpl implements TransactionsLogService {

    private final TransactionLogRepository transactionLogRepository;

    public TransactionsLogServiceImpl(TransactionLogRepository transactionLogRepository) {
        this.transactionLogRepository = transactionLogRepository;
    }

    @Override
    public TransactionsLog saveTransactionLog(TransactionsLog transactionsLog) {
        log.info("Save Transaction log {} ", transactionsLog);
        return transactionLogRepository.save(transactionsLog);
    }
}
