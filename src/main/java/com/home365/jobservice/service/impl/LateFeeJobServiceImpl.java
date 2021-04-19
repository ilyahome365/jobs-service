package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.LateFee;
import com.home365.jobservice.entities.LocationRules;
import com.home365.jobservice.entities.Rules;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.LateFeeConfiguration;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.LocationRulesService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LateFeeJobServiceImpl extends JobExecutorImpl {
    public static final String LATE_FEE_JOB = "Late Fee Job";
    public static final String LATE_FEE_JOB_FINISHED = "Late Fee Job Finished";

    private final JobsConfigurationService jobsConfigurationService;
    private final TransactionsService transactionsService;
    private final LocationRulesService locationRulesService;
    private final ObjectMapper objectMapper;

    public LateFeeJobServiceImpl(AppProperties appProperties,
                                 MailService mailService,
                                 JobsConfigurationService jobsConfigurationService,
                                 TransactionsService transactionsService, LocationRulesService locationRulesService, ObjectMapper objectMapper) {
        super(appProperties, mailService);
        this.jobsConfigurationService = jobsConfigurationService;
        this.transactionsService = transactionsService;
        this.locationRulesService = locationRulesService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getJobName() {
        return LATE_FEE_JOB;
    }

    @Override
    public String execute(String locationId) throws Exception {
        LocationRules locationRules = this.locationRulesService.findLocationRulesById(locationId).get();
        Rules rules = null;
        try {
            rules = objectMapper.readValue(locationRules.getRules(), Rules.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            rules = new Rules();
        }
        locationRules.setRule(rules);
        LateFeeConfiguration lateFeeConfiguration = jobsConfigurationService.getLateFeeConfiguration();

        List<Transactions> candidateTransactionsWithNoLateFee = transactionsService.findAllByBillTypeAndStatus(lateFeeConfiguration.getCategoryNames(), lateFeeConfiguration.getStatus(), locationRules.getPmAccountId());

        candidateTransactionsWithNoLateFee.forEach(transactions -> log.info("{}, {}, {}, {}, {}, {} ", transactions.getAmount() / 100, transactions.getDueDate(), transactions.getTransactionId(), transactions.getStatus(),
                transactions.getCategoryName(), transactions.getChargeAccountId()));


        List<Transactions> lateFeeTransactions = createLateFeeTransactions(
                lateFeeConfiguration,
                candidateTransactionsWithNoLateFee,
                locationRules
        );
        showSummary(lateFeeTransactions);
        log.info(LATE_FEE_JOB_FINISHED);
        return LATE_FEE_JOB_FINISHED;
    }

    private List<Transactions> createLateFeeTransactions(LateFeeConfiguration lateFeeConfiguration,
                                                         List<Transactions> candidateTransactionsWithNoLateFee, LocationRules locationRules) {

        ILateFeeAdditionalInformationProjection lateFeeAdditionalInformationProjection = transactionsService.getLateFeeAdditionalInformation();

        Rules rule = locationRules.getRule();
        LateFee lateFees = rule.getLateFees();
        List<Transactions> feeTransactions = new ArrayList<>();
        candidateTransactionsWithNoLateFee.forEach(transactions -> {
            long ownerAmount = getAmount(lateFees.getOwnerPercentage(), lateFeeConfiguration, transactions);
            if (ownerAmount > 0) {
                log.info("Owner amount : {} for transaction : {} and owner id : {}", ownerAmount, transactions.getTransactionId(), transactions.getReceiveAccountId());
                Transactions feeTransaction = createTransaction(lateFeeAdditionalInformationProjection, transactions, ownerAmount, transactions.getReceiveAccountId());
                feeTransactions.add(feeTransaction);
            }
            long feeAccountManagerPercentage = getAmount(lateFees.getAccountManagerPercentage(), lateFeeConfiguration, transactions);
            if (feeAccountManagerPercentage > 0) {
                log.info("Pm fee amount : {}  for transaction : {} and owner id : {} ", feeAccountManagerPercentage, transactions.getTransactionId(), lateFees.getPmAccount());
                Transactions feeTransaction = createTransaction(lateFeeAdditionalInformationProjection, transactions, feeAccountManagerPercentage, lateFees.getPmAccount());
                feeTransactions.add(feeTransaction);
            }
        });

        return transactionsService.saveAllTransactions(feeTransactions);
    }


    private long getAmount(Float percentage, LateFeeConfiguration lateFeeConfiguration, Transactions transactions) {
        double amount = lateFeeConfiguration.getFeeAmount();
        long feeAmount = 0;
        feeAmount = (long) (transactions.getAmount() * amount);
        feeAmount = (long) (feeAmount * (percentage / 100.0f));
        return feeAmount;
    }

    private Transactions createTransaction(ILateFeeAdditionalInformationProjection lateFeeAdditionalInformationProjection, Transactions transactions, Long feeAmount,
                                           String receiveAccountId) {
        String transactionId = transactions.getTransactionId();

        Transactions feeTransaction = new Transactions();
        feeTransaction.setTransactionId(UUID.randomUUID().toString());
        feeTransaction.setBillType("lateFee");
        feeTransaction.setAccountingTypeId(lateFeeAdditionalInformationProjection.getAccountingTypeId());
        feeTransaction.setCategoryId(lateFeeAdditionalInformationProjection.getCategoryId());
        feeTransaction.setCategoryName(lateFeeAdditionalInformationProjection.getCategoryName());
        feeTransaction.setAccountingName(lateFeeAdditionalInformationProjection.getAccountingName());
        feeTransaction.setTransactionType("Charge");
        feeTransaction.setChargeAccountId(transactions.getChargeAccountId());
        feeTransaction.setReceiveAccountId(receiveAccountId);
        feeTransaction.setPmAccountId(transactions.getPmAccountId());
        feeTransaction.setPropertyId(transactions.getPropertyId());
        feeTransaction.setAmount(feeAmount);
        feeTransaction.setAmountBeforeDiscount(feeAmount);
        feeTransaction.setStatus("readyForPayment");
        feeTransaction.setChargedBy("Home365");
        feeTransaction.setReferenceTransactionId(transactionId);
        feeTransaction.setDueDate(new Timestamp(new Date().getTime()));
        return feeTransaction;
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
