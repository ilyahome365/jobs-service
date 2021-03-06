package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.projection.IOwnerRentNotification;
import com.home365.jobservice.repository.TransactionsRepository;
import com.home365.jobservice.repository.TransactionsWithProjectedBalanceRepo;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final TransactionsWithProjectedBalanceRepo transactionsWithProjectedBalanceRepo;

    @PersistenceContext
    private EntityManager entityManager;


    public TransactionsServiceImpl(TransactionsRepository transactionsRepository, TransactionsWithProjectedBalanceRepo transactionsWithProjectedBalanceRepo) {
        this.transactionsRepository = transactionsRepository;
        this.transactionsWithProjectedBalanceRepo = transactionsWithProjectedBalanceRepo;
    }

    @Override
    public List<Transactions> saveAllTransactions(List<Transactions> transactions) {
        return transactionsRepository.saveAll(transactions);
    }

    @Override
    public List<TransactionsWithProjectedBalance> getTransactionsWithProjectedBalance(String cycleDate, String locationId) {
        log.info("Enter to get all transactions for cycle : {}", cycleDate);
        return transactionsWithProjectedBalanceRepo.getTransactionsWithProjectedBalance(cycleDate, locationId);
    }

    @Override
    public List<Transactions> findAllByBillTypeAndStatus(List<String> categoryNames, List<String> status, String pmAccountId, String from
            , String to) {
        return transactionsRepository.findAllByBillTypeAndStatus(categoryNames, status, pmAccountId,from,to);
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
    public Transactions save(Transactions transaction) {
        return transactionsRepository.save(transaction);
    }

    @Override
    public void saveTransactionsWithBalance(TransactionsWithProjectedBalance transactionsWithProjectedBalance) {
        log.info("save transactions with balance : {} ", transactionsWithProjectedBalance);
        transactionsWithProjectedBalanceRepo.save(transactionsWithProjectedBalance);
    }

    @Override
    public Optional<Transactions> findTransaction(String transactionId) {
        return transactionsRepository.findById(transactionId);

    }

    @Override
    public void saveTransaction(Transactions transactions) {
        log.info("Save transaction id : {} ", transactions.getTransactionId());
        transactionsRepository.save(transactions);
    }

    @Override
    public void saveAllTransactionsWithProjectedBalance(List<TransactionsWithProjectedBalance> transactionsWithProjectedBalances) {
        log.info("Save all transactions : {} ", transactionsWithProjectedBalances);
        transactionsWithProjectedBalanceRepo.saveAll(transactionsWithProjectedBalances);
    }

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        entityManager.detach(newEntity);
        return this.findById(newEntity.idOfEntity()).orElse(null);
    }

    @Override
    public Optional<Transactions> findById(String accountId) {
        return transactionsRepository.findById(accountId);
    }

    @Override
    public void saveAll(List<Transactions> transactions) {
        transactionsRepository.saveAll(transactions);
    }

    @Override
    public List<IOwnerRentNotification> getTransactionsForOwnerRent(String pmAccount, String firstDate, String lastDate) {
        return transactionsRepository.getOwnerRentNotification(pmAccount, firstDate, lastDate);
    }


}
