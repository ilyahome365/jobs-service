package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.LateFeeJobService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class LateFeeJobServiceImpl implements LateFeeJobService {
    private final TransactionsService transactionsService;
    private final ReentrantLock lock = new ReentrantLock();

    public LateFeeJobServiceImpl(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @Override
    public boolean startLateFeeJob() {
        log.info("Try to Start Late Fee Job");
        if (lock.tryLock()) {
            try {
                log.info("Late Fee Job Started");
                int lateFeeRetro = -1;
                double feeAmount = 5;
                double feeAmountPercentage = feeAmount / 100;
                List<String> billTypes = Collections.singletonList("Rent");
                List<String> status = Collections.singletonList("readyForPayment");
                List<Transactions> candidateTransactionsWithNoLateFee = findTransactions(lateFeeRetro, billTypes, status);
                List<Transactions> lateFeeTransactions = createLateFeeTransactions(feeAmountPercentage, candidateTransactionsWithNoLateFee);
                showSummary(lateFeeTransactions);
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                lock.unlock();
            }
            log.info("Late Fee Job Finished");
            return true;
        }

        log.info("Late Fee Job didn't Start -> Already Running");
        return false;
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
