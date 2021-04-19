package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.*;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.PendingStatusJobData;
import com.home365.jobservice.model.TransactionsFailedToChange;
import com.home365.jobservice.service.*;
import com.home365.jobservice.utils.Converters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChangeBillStatusServiceImpl extends JobExecutorImpl {
    public static final String NO_PROJECTED_BALANCE_FOR_THIS_ACCOUNT = "No Projected Balance for this account :";
    public static final String CANT_CHANGE_THIS_TRANSACTION = " : Cant change this transaction ";
    private final String JOB_PENDING_DUE = "ChangeBillStatusJob";

    private final TransactionsService transactionsService;
    private final JobLogService jobLogService;
    private final TransactionsLogService transactionsLogService;
    private final JobsConfigurationService jobsConfigurationService;


    public ChangeBillStatusServiceImpl(AppProperties appProperties, MailService mailService, TransactionsService transactionsService, JobLogService jobLogService, TransactionsLogService transactionsLogService, JobsConfigurationService jobsConfigurationService) {
        super(appProperties, mailService);
        this.transactionsService = transactionsService;
        this.jobLogService = jobLogService;
        this.transactionsLogService = transactionsLogService;
        this.jobsConfigurationService = jobsConfigurationService;
    }

    @Override
    protected String getJobName() {
        return JOB_PENDING_DUE;
    }

    @Override
    protected String execute(String locationId) throws JsonProcessingException {
        log.info("Enter to pendingStatusChange on date {}", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ChangeBillConfiguration changeBillConf = jobsConfigurationService.getChangeBillConfiguration(locationId);
        PendingStatusJobData pendingStatusJobData = new PendingStatusJobData();

        String cycleDate = createNextCycleDate();
        List<TransactionsWithProjectedBalance> transactions = transactionsService.getTransactionsWithProjectedBalance(cycleDate, locationId);
        List<String> accounts = transactions.parallelStream().map(TransactionsWithProjectedBalance::getChargeAccountId).distinct().collect(Collectors.toList());
        List<TransactionsWithProjectedBalance> failedTransactions = new ArrayList<>();
        List<TransactionsWithProjectedBalance> transactionsPerAccount = new ArrayList<>();
        for (String account : accounts) {
            log.info("get transactions for account {}", account);
            try {
                transactionsPerAccount = transactions.parallelStream().
                        filter(transactions1 -> transactions1.getChargeAccountId()
                                .equals(account)).
                        sorted(Comparator.comparing(TransactionsWithProjectedBalance::getDueDate).thenComparing(Comparator.comparing(TransactionsWithProjectedBalance::getAmount).reversed()))
                        .collect(Collectors.toList());

                transactionsPerAccount = changeStatusByAmount(transactionsPerAccount, failedTransactions, pendingStatusJobData, changeBillConf);
                List<Transactions> transactionToSave = createTransactionFromTransactionsWithProjectBalance(transactionsPerAccount);
                saveAllTransactions(transactionToSave, pendingStatusJobData);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
                pendingStatusJobData.setFailedToChange(pendingStatusJobData.getFailedToChange() + transactionsPerAccount.size());
            }

        }
        try {
            createJobLog(pendingStatusJobData, cycleDate);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return String.format("%s : Finished get  : %d transactions  until date %s with results : %s ", getJobName(), transactions.size(), cycleDate, pendingStatusJobData.toString());
    }


    private List<Transactions> createTransactionFromTransactionsWithProjectBalance(List<TransactionsWithProjectedBalance> transactionsPerAccount) {
        List<Transactions> transactions = new ArrayList<>();
        for (TransactionsWithProjectedBalance transactionsWithProjectedBalance : transactionsPerAccount) {

            Optional<Transactions> transaction = transactionsService.findTransaction(transactionsWithProjectedBalance.getTransactionId());
            if (transaction.isPresent()) {
                transaction.get().setStatus(transactionsWithProjectedBalance.getStatus());
                transactions.add(transaction.get());
            }
        }
        return transactions;
    }

    private void saveAllTransactions(List<Transactions> transactionsToSave, PendingStatusJobData pendingStatusJobData) {
        for (Transactions transactions : transactionsToSave) {
            try {
                Transactions transactionAfterSave = transactionsService.save(transactions);
                saveStatistics(transactionAfterSave, pendingStatusJobData);
            } catch (Exception e) {
                log.error(Arrays.toString(e.getStackTrace()));
                log.error(e.getMessage());
                createTransactionLog(transactions);
                pendingStatusJobData.setFailedToChange(pendingStatusJobData.getFailedToChange() + 1);
                TransactionsFailedToChange transactionsFailedToChange = new TransactionsFailedToChange();
                transactionsFailedToChange.setTransactionId(transactions.getTransactionId());
                transactionsFailedToChange.setReasonFailedToChange(e.getMessage());
                pendingStatusJobData.getTransactionsFailedToChanges().add(transactionsFailedToChange);
            }
        }
    }

    private void saveStatistics(Transactions transactionAfterSave, PendingStatusJobData pendingStatusJobData) {
        String transactionStatus = transactionAfterSave.getStatus();
        if (transactionStatus.equals(TransactionType.readyForPayment.name()))
            pendingStatusJobData.setReadyForPayment(pendingStatusJobData.getReadyForPayment() + 1);
        else if (transactionStatus.equals(TransactionType.pendingContribution.name()))
            pendingStatusJobData.setPendingContribution(pendingStatusJobData.getPendingContribution() + 1);
    }


    private void createJobLog(PendingStatusJobData pendingStatusJobData, String cycleDate) {
        log.info("create job log with ready for payment : {}  , pendingContribution: {} , with cycle date : {} "
                , pendingStatusJobData.getReadyForPayment(), pendingStatusJobData.getPendingContribution(), cycleDate);
        LocalDate localDate = LocalDate.now();

        Date date = new Date();
        Timestamp currentTimeAndDate = new Timestamp(date.getTime());
        JobLog jobLog = new JobLog();
        jobLog.setJobName(JOB_PENDING_DUE);
        jobLog.setLastRun(localDate);
        jobLog.setDate(currentTimeAndDate);
        String jobComment = "job " + JOB_PENDING_DUE + " run - readyForPayment : "
                + pendingStatusJobData.getReadyForPayment() + " , pendingContribution : " + pendingStatusJobData.getPendingContribution() + ", with cycle date : " + cycleDate;
        jobLog.setComments(jobComment);
        jobLogService.saveJobLog(jobLog);
    }

    private List<TransactionsWithProjectedBalance> changeStatusByAmount(List<TransactionsWithProjectedBalance> transactions, List<TransactionsWithProjectedBalance> failedTransactions, PendingStatusJobData pendingStatusJobData, ChangeBillConfiguration changeBillConf) {
        double billBalance = 0.0;

        for (TransactionsWithProjectedBalance transaction : transactions) {
            try {
                billBalance = billBalance + transaction.getAmount();
                double projectedBalanceWithTrashHold = changeBillConf.getTrashHold() + transaction.getProjected_balance().doubleValue();
                log.info("transaction id {} with billBalance {}   and projected balance {}", transaction.getTransactionId(), billBalance, projectedBalanceWithTrashHold);
                if (projectedBalanceWithTrashHold >= billBalance) {
                    log.info("Change transaction {} to readyForPayment", transaction.getTransactionId());
                    transaction.setStatus(TransactionType.readyForPayment.name());

                } else {
                    log.info("Change transaction {} to pendingContribution", transaction.getTransactionId());
                    transaction.setStatus(TransactionType.pendingContribution.name());
                }
            } catch (Exception e) {
                log.error("Failed to change transaction {}  for account {}  ", transaction.getTransactionId(), transaction.getChargeAccountId());
                log.error("the Exception reason : " + e.getLocalizedMessage());
                failedTransactions.add(transaction);
                createTransactionLog(Converters.fromTransactionsWithProjectedBalanceToTransactions(transaction));
                pendingStatusJobData.setFailedToChange(pendingStatusJobData.getFailedToChange() + 1);
                TransactionsFailedToChange transactionsFailedToChange = new TransactionsFailedToChange();
                transactionsFailedToChange.setTransactionId(transaction.getTransactionId());
                transactionsFailedToChange.setReasonFailedToChange(NO_PROJECTED_BALANCE_FOR_THIS_ACCOUNT + transaction.getChargeAccountId());
                pendingStatusJobData.getTransactionsFailedToChanges().add(transactionsFailedToChange);
            }
        }
        return transactions;
    }

    private void createTransactionLog(Transactions transaction) {
        LocalDate localDate = LocalDate.now();
        TransactionsLog transactionsLog = new TransactionsLog();
        transactionsLog.setTransactionId(transaction.getTransactionId());
        transactionsLog.setArgument(JOB_PENDING_DUE + CANT_CHANGE_THIS_TRANSACTION + transaction.getTransactionId());
        transactionsLog.setEventName(7);
        transactionsLog.setContactAccountId(transaction.getChargeAccountId());
        transactionsLog.setTransactionLogId(UUID.randomUUID().toString());
        transactionsLog.setDate(localDate);
        transactionsLogService.saveTransactionLog(transactionsLog);

    }

    private String createNextCycleDate() {
        String dayOfNextCycle = "14";
        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfMonth() > 14)
            now = now.plusMonths(2);
        else
            now = now.plusMonths(1);
        String month = String.valueOf(now.getMonth().getValue());
        String year = String.valueOf(now.getYear());
        return year + "-" + month + "-" + dayOfNextCycle;

    }

}
