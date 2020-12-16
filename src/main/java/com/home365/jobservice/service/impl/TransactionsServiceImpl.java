package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.repository.TransactionsRepository;
import com.home365.jobservice.repository.TransactionsWithProjectedBalanceRepo;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final TransactionsWithProjectedBalanceRepo transactionsWithProjectedBalanceRepo;

    public TransactionsServiceImpl(TransactionsRepository transactionsRepository, TransactionsWithProjectedBalanceRepo transactionsWithProjectedBalanceRepo) {
        this.transactionsRepository = transactionsRepository;
        this.transactionsWithProjectedBalanceRepo = transactionsWithProjectedBalanceRepo;
    }

    @Override
    public List<Transactions> saveAllTransactions(List<Transactions> transactions) {
        log.info("enter to save All Transactions {} ", transactions);
        return transactionsRepository.saveAll(transactions);
    }

    @Override
    public List<TransactionsWithProjectedBalance> getTransactionsWithProjectedBalance(String cycleDate) {
        log.info("Enter to get all transactions for cycle : {}", cycleDate);
        return transactionsWithProjectedBalanceRepo.getTransactionsWithProjectedBalance(cycleDate);
    }

    @Override
    public List<Transactions> findAllByBillTypeAndStatus(List<String> categoryNames, List<String> status) {
        return transactionsRepository.findAllByBillTypeAndStatus(categoryNames, status);
    }

    @Override
    public List<Transactions> findByRecurringTemplateId(String recurringTemplateId) {
        return transactionsRepository.findByRecurringTemplateId(recurringTemplateId);
    }

    @Override
    public ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation() {
        return transactionsRepository.getLateFeeAdditionalInformation();
    }

    @Override
    public List<Transactions> findTenantRentTransactionsByPropertyId(String propertyId) {
        return transactionsRepository.findTenantRentTransactionsByPropertyId(propertyId);
    }

    @Override
    public void save(Transactions transaction) {
        transactionsRepository.save(transaction);
    }
}
