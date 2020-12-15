package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsLog;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.PendingStatusJobData;
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
    protected String execute()  {
        log.info("Enter to pendingStatusChange on date {}", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        PendingStatusJobData pendingStatusJobData = new PendingStatusJobData();

        String cycleDate = createNextCycleDate();
        List<TransactionsWithProjectedBalance> transactions = transactionsService.getTransactionsWithProjectedBalance(cycleDate);
        List<String> accounts = transactions.parallelStream().map(TransactionsWithProjectedBalance::getChargeAccountId).distinct().collect(Collectors.toList());
//        transactions.stream().map(TransactionsWithProjectedBalance::getTransactions).map(Transactions::getChargeAccountId).
        List<TransactionsWithProjectedBalance> failedTransactions = new ArrayList<>();
        for (String account : accounts) {
            log.info("get transactions for account {}", account);
            List<Transactions> transactionsToSave = new ArrayList<>();
            try {
                List<TransactionsWithProjectedBalance> transactionsPerAccount = transactions.parallelStream().
                        filter(transactions1 -> transactions1.getChargeAccountId()
                                .equals(account)).
                        sorted(Comparator.comparing(TransactionsWithProjectedBalance::getDueDate).thenComparing(Comparator.comparing(TransactionsWithProjectedBalance::getAmount).reversed()))

                        .collect(Collectors.toList());

                transactionsPerAccount = changeStatusByAmount(transactionsPerAccount, failedTransactions, pendingStatusJobData);
                transactionsToSave = transactionsPerAccount.stream().map(Converters::fromTransactionsWithProjectedBalanceToTransactions).collect(Collectors.toList());
                transactionsService.saveAllTransactions(transactionsToSave);

            } catch (Exception e) {
                log.error(e.getMessage());
                pendingStatusJobData.setFailedToChange(pendingStatusJobData.getFailedToChange() + transactionsToSave.size());


            }

        }
        createJobLog(pendingStatusJobData, cycleDate);

        return String.format("[%s] : Finished for cycleDate [%s] with results : [%s] ", getJobName(), cycleDate, pendingStatusJobData.toString());
    }


    private void createJobLog(PendingStatusJobData pendingStatusJobData, String cycleDate) {
        log.info("create job log with ready for payment : {}  , pendingContribution: {} , with cycle date : {} "
                , pendingStatusJobData.getReadyForPayment(), pendingStatusJobData.getPendingContribution(), cycleDate);
        LocalDate localDate = LocalDate.now();
        Date date = new Date();
        Timestamp currentTimeAndDate = new Timestamp(date.getTime());
        JobLog jobLog = new JobLog();
//        jobLog.setId(UUID.randomUUID().toString());
        jobLog.setJobName(JOB_PENDING_DUE);
        jobLog.setLastRun(localDate);
        jobLog.setDate(currentTimeAndDate);
        String jobComment = "job " + JOB_PENDING_DUE + " run - readyForPayment : "
                + pendingStatusJobData.getReadyForPayment() + " , pendingContribution : " + pendingStatusJobData.getPendingContribution() + ", with cycle date : " + cycleDate;
        jobLog.setComments(jobComment);
        jobLogService.saveJobLog(jobLog);
    }

    private List<TransactionsWithProjectedBalance> changeStatusByAmount(List<TransactionsWithProjectedBalance> transactions, List<TransactionsWithProjectedBalance> failedTransactions, PendingStatusJobData pendingStatusJobData) {
        double billBalance = 0.0;

        for (TransactionsWithProjectedBalance transaction : transactions) {
            try {
                billBalance = billBalance + transaction.getAmount();
                double projectedBalanceWithTrashHold = appProperties.getTrashHold() + transaction.getProjected_balance();
                log.info("transaction id {} with billBalance {}   and projected balance {}", transaction.getTransactionId(), billBalance, projectedBalanceWithTrashHold);
                if (projectedBalanceWithTrashHold >= billBalance) {
                    log.info("Change transaction {} to readyForPayment", transaction.getTransactionId());
                    transaction.setStatus(TransactionType.readyForPayment.name());

                    pendingStatusJobData.setReadyForPayment(pendingStatusJobData.getReadyForPayment() + 1);
                } else {
                    log.info("Change transaction {} to pendingContribution", transaction.getTransactionId());
                    transaction.setStatus(TransactionType.pendingContribution.name());
                    pendingStatusJobData.setPendingContribution(pendingStatusJobData.getPendingContribution() + 1);
                }
            } catch (Exception e) {
                log.error("Failed to change transaction {}  for account {}  ", transaction.getTransactionId(), transaction.getChargeAccountId());
                log.error("the Exception reason : " + e.getLocalizedMessage());
                failedTransactions.add(transaction);
                createTransactionLog(Converters.fromTransactionsWithProjectedBalanceToTransactions(transaction));
                pendingStatusJobData.setFailedToChange(pendingStatusJobData.getFailedToChange() + 1);
            }
        }
        return transactions;
    }

    private void createTransactionLog(Transactions transaction) {
        LocalDate localDate = LocalDate.now();
        TransactionsLog transactionsLog = new TransactionsLog();
        transactionsLog.setTransactionId(transaction.getTransactionId());
        transactionsLog.setArgument(JOB_PENDING_DUE + " : Cant change this transaction " + transaction.getTransactionId());
        transactionsLog.setEventName(7);
        transactionsLog.setContactAccountId(transaction.getChargeAccountId());
        transactionsLog.setTransactionLogId(UUID.randomUUID().toString());
        transactionsLog.setDate(localDate);
        transactionsLogService.saveTransactionLog(transactionsLog);

    }

    private String createNextCycleDate() {
        String dayOfNextCycle = "14";
        LocalDateTime now = LocalDateTime.now().plusMonths(1);
        String month = String.valueOf(now.getMonth().getValue());
        String year = String.valueOf(now.getYear());
        return year + "-" + month + "-" + dayOfNextCycle;

    }

}
