package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.executor.JobExecutionResult;
import com.home365.jobservice.executor.JobService;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class LateFeeJobServiceImpl implements JobService {
    private final JobsConfigurationService jobsConfigurationService;
    private final TransactionsService transactionsService;
    private final ReentrantLock lock = new ReentrantLock();

    public LateFeeJobServiceImpl(JobsConfigurationService jobsConfigurationService,
                                 TransactionsService transactionsService) {
        this.jobsConfigurationService = jobsConfigurationService;
        this.transactionsService = transactionsService;
    }

    @Override
    public JobExecutionResult executeJob() throws Exception{
        JobExecutionResult jobExecutionResult = new JobExecutionResult();
        log.info("Try to Start Late Fee Job");
        if (lock.tryLock()) {
            try {
                LateFeeConfiguration lateFeeConfiguration = jobsConfigurationService.getLateFeeConfiguration();
                log.info("Late Fee Job Started");
                List<String> billTypes = Collections.singletonList("Rent");
                List<String> status = Collections.singletonList("readyForPayment");
                List<Transactions> candidateTransactionsWithNoLateFee = findTransactions(
                        lateFeeConfiguration.getLateFeeRetro(),
                        billTypes,
                        status
                );
                List<Transactions> lateFeeTransactions = createLateFeeTransactions(lateFeeConfiguration.getFeeAmount(), candidateTransactionsWithNoLateFee);
                showSummary(lateFeeTransactions);
            } finally {
                lock.unlock();
            }
            log.info("Late Fee Job Finished");
            jobExecutionResult.setSucceeded(true);
            jobExecutionResult.setMessage("Late Fee Job Finished");
        } else {
            log.info("Late Fee Job didn't Start -> Already Running");
            jobExecutionResult.setSucceeded(false);
            jobExecutionResult.setError("Late Fee Job didn't Start -> Already Running");
        }
        return jobExecutionResult;
    }

    private List<Transactions> findTransactions(int lateFeeRetro,
                                                List<String> billTypes,
                                                List<String> status) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, lateFeeRetro);
        java.sql.Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
        return transactionsService.findAllByBillTypeAndStatusAndDueDateBefore(billTypes, status, timestamp);
    }

    private List<Transactions> createLateFeeTransactions(double feeAmountPercentage,
                                                         List<Transactions> candidateTransactionsWithNoLateFee) {
        List<Transactions> feeTransactions = new ArrayList<>();
        candidateTransactionsWithNoLateFee.forEach(transactions -> {
            String transactionId = transactions.getTransactionId();
            long feeAmount = (long) (transactions.getAmount() * feeAmountPercentage);

            Transactions feeTransaction = new Transactions();
            feeTransaction.setBillType("lateFee");
            feeTransaction.setAccountingTypeId(transactions.getAccountingTypeId());
            feeTransaction.setCategoryId(transactions.getCategoryId());
            feeTransaction.setChargeAccountId(transactions.getChargeAccountId());
            feeTransaction.setReceiveAccountId(transactions.getReceiveAccountId());
            feeTransaction.setPmAccountId(transactions.getPmAccountId());
            feeTransaction.setPropertyId(transactions.getPropertyId());
            feeTransaction.setAmount(feeAmount);
            feeTransaction.setStatus("readyForPayment");
            feeTransaction.setReferenceTransactionId(transactionId);
            feeTransaction.setDueDate(new Timestamp(new Date().getTime()));
            feeTransactions.add(feeTransaction);
        });

        return transactionsService.saveAllTransactions(feeTransactions);
    }

    private void showSummary(List<Transactions> lateFeeTransactions) {
        String comment = "Fee Transactions Created Summary: ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(comment).append("\n");
        log.info(comment);
        lateFeeTransactions.forEach(transactions -> {
            String comment1 = String.format("Transaction: Id [%s], Amount [%d], Status [%s], Due Date [%s], Reference Transaction Id [%s]",
                    transactions.getTransactionId(),
                    transactions.getAmount(),
                    transactions.getStatus(),
                    transactions.getDueDate().toString(),
                    transactions.getReferenceTransactionId()
            );
            log.info(comment1);
            stringBuilder.append(comment1).append("\n");
        });
        String comments = stringBuilder.toString();
    }
}
