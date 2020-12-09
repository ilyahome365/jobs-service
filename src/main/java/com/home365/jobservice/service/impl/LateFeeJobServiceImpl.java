package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LateFeeJobServiceImpl extends JobExecutorImpl {
    public static final String LATE_FEE_JOB = "Late Fee Job";

    private final JobsConfigurationService jobsConfigurationService;
    private final TransactionsService transactionsService;

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
        LateFeeConfiguration lateFeeConfiguration = jobsConfigurationService.getLateFeeConfiguration();

        List<Transactions> candidateTransactionsWithNoLateFee = transactionsService.findAllByBillTypeAndStatus(
                lateFeeConfiguration.getCategoryNames(),
                lateFeeConfiguration.getStatus()
        );

        List<Transactions> lateFeeTransactions = createLateFeeTransactions(
                lateFeeConfiguration,
                candidateTransactionsWithNoLateFee
        );
        showSummary(lateFeeTransactions);
        log.info("Late Fee Job Finished");
        return "Late Fee Job Finished";
    }

    private List<Transactions> createLateFeeTransactions(LateFeeConfiguration lateFeeConfiguration,
                                                         List<Transactions> candidateTransactionsWithNoLateFee) {

        ILateFeeAdditionalInformationProjection lateFeeAdditionalInformationProjection = transactionsService.getLateFeeAdditionalInformation();

        List<Transactions> feeTransactions = new ArrayList<>();
        candidateTransactionsWithNoLateFee.forEach(transactions -> {
            String transactionId = transactions.getTransactionId();

            double amount = lateFeeConfiguration.getFeeAmount();
            double maxFeeAmount = lateFeeConfiguration.getMaxFeeAmount();

            long feeAmount = 0;
            if (transactions.getAmount() * amount >= maxFeeAmount) {
                feeAmount = (long) maxFeeAmount;
            } else {
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
            feeTransaction.setChargedBy("Home365");
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
        log.info(comments);
    }
}
