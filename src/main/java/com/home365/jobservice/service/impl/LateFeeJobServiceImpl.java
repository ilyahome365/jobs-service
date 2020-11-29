package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.LateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class LateFeeJobServiceImpl extends JobExecutorImpl {
    public static final String LATE_FEE_JOB = "Late Fee Job";
    private final JobsConfigurationService jobsConfigurationService;
    private final TransactionsService transactionsService;
    private final ReentrantLock lock = new ReentrantLock();

    public LateFeeJobServiceImpl(AppProperties appProperties,
                                 MailService mailService,
                                 JobsConfigurationService jobsConfigurationService,
                                 TransactionsService transactionsService) {
        super(appProperties, mailService);
        this.jobsConfigurationService = jobsConfigurationService;
        this.transactionsService = transactionsService;
    }

    @Override
    protected String getJobName() {
        return LATE_FEE_JOB;
    }

    @Override
    public String execute() throws Exception {
        log.info("Try to Start Late Fee Job");
        if (lock.tryLock()) {
            try {
                LateFeeConfiguration lateFeeConfiguration = jobsConfigurationService.getLateFeeConfiguration();
                log.info("Late Fee Job Started");
                List<Transactions> candidateTransactionsWithNoLateFee = findTransactions(
                        lateFeeConfiguration.getLateFeeRetro(),
                        lateFeeConfiguration.getBillTypes(),
                        lateFeeConfiguration.getStatus()
                );
                List<Transactions> lateFeeTransactions = createLateFeeTransactions(
                        lateFeeConfiguration,
                        candidateTransactionsWithNoLateFee
                );
                showSummary(lateFeeTransactions);
            } finally {
                lock.unlock();
            }
            log.info("Late Fee Job Finished");
            return "Late Fee Job Finished";
        } else {
            log.info("Late Fee Job didn't Start -> Already Running");
            return "Late Fee Job didn't Start -> Already Running";
        }
    }

    private List<Transactions> findTransactions(int lateFeeRetro,
                                                List<String> billTypes,
                                                List<String> status) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, lateFeeRetro);
        return transactionsService.findAllByBillTypeAndStatusAndDueDateBefore(billTypes, status, calendar.getTime());
    }

    private List<Transactions> createLateFeeTransactions(LateFeeConfiguration lateFeeConfiguration,
                                                         List<Transactions> candidateTransactionsWithNoLateFee) {

        LateFeeAdditionalInformationProjection lateFeeAdditionalInformationProjection = transactionsService.getLateFeeAdditionalInformation();

        List<Transactions> feeTransactions = new ArrayList<>();
        candidateTransactionsWithNoLateFee.forEach(transactions -> {
            String transactionId = transactions.getTransactionId();

            double amount = lateFeeConfiguration.getFeeAmount();
            double maxFeeAmount = lateFeeConfiguration.getMaxFeeAmount();

            long feeAmount = 0;
            if(transactions.getAmount() * amount >= maxFeeAmount){
                feeAmount = (long) maxFeeAmount;
            }else{
                feeAmount = (long) (transactions.getAmount() * amount);
            }

            Transactions feeTransaction = new Transactions();
            feeTransaction.setBillType("lateFee");
            feeTransaction.setAccountingTypeId(lateFeeAdditionalInformationProjection.getAccountingTypeId());
            feeTransaction.setCategoryId(lateFeeAdditionalInformationProjection.getCategoryId());
            feeTransaction.setCategoryName(lateFeeAdditionalInformationProjection.getCategoryName());
            feeTransaction.setAccountingName(lateFeeAdditionalInformationProjection.getAccountingName());
            feeTransaction.setTransactionType("Charge");
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
