package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.service.LateFeeJobService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Service
public class LateFeeJobServiceImpl implements LateFeeJobService {

    private final boolean NOT_RUNNING = false;
    private final boolean RUNNING = true;

    private final long feeAmount = 5;

    private final TransactionsService transactionsService;
    private final AtomicBoolean isJobStarted;

    public LateFeeJobServiceImpl(TransactionsService transactionsService) {
        this.isJobStarted = new AtomicBoolean(false);
        this.transactionsService = transactionsService;
    }

    @Override
    public boolean startLateFeeJob() {
        log.info("Try to Start Late Fee Job");
        if (isJobStarted.compareAndSet(NOT_RUNNING, RUNNING)) {
            log.info("Late Fee Job Started");
            List<Transactions> candidateTransactionsWithNoLateFee = findTransactions();
            long feeAmountPercentage = feeAmount / 100;
            List<Transactions> lateFeeTransactions = createLateFeeTransactions(candidateTransactionsWithNoLateFee, feeAmountPercentage);
            showSummary(lateFeeTransactions);
            log.info("Late Fee Job Finished");
            return true;
        }
        log.info("Late Fee Job didn't Start -> Already Running");
        return false;
    }

    private List<Transactions> findTransactions() {
        // Type: Rent
        // Status: readyForPayment
        // DueDate: now - 1 Month

        List<String> billTypes = Collections.singletonList("Rent");
        List<String> status = Collections.singletonList("readyForPayment");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, -1);
        return transactionsService.findAllByBillTypeAndStatusAndDueDateBefore(billTypes, status, calendar.getTime());
    }

    private List<Transactions> createLateFeeTransactions(List<Transactions> candidateTransactionsWithNoLateFee, long feeAmountPercentage) {

        List<Transactions> feeTransactions = new ArrayList<>();
        candidateTransactionsWithNoLateFee.forEach(transactions -> {
            String transactionId = transactions.getTransactionId();
            long feeAmount = transactions.getAmount() * feeAmountPercentage;

            Transactions feeTransaction = new Transactions();
            feeTransaction.setBillType("lateFee");
            feeTransaction.setAmount(feeAmount);
            feeTransaction.setStatus("readyForPayment");
            feeTransaction.setReferenceTransactionId(transactionId);
            feeTransaction.setDueDate(new Timestamp(new Date().getTime()));
            feeTransactions.add(feeTransaction);
        });

        // Save
        return feeTransactions;
    }

    private void showSummary(List<Transactions> lateFeeTransactions) {
        log.info("Fee Transactions Created:");
        lateFeeTransactions.forEach(new Consumer<Transactions>() {
            @Override
            public void accept(Transactions transactions) {
                log.info(String.format("Transaction: Id [%s], Amount [%d], Status [%s], Due Date [%s], Reference Transaction Id [%s]",
                        transactions.getTransactionId(),
                        transactions.getAmount(),
                        transactions.getStatus(),
                        transactions.getDueDate().toString(),
                        transactions.getReferenceTransactionId()
                ));
            }
        });
    }
}
