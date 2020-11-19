package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsLog;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.model.PendingStatusJobData;
import com.home365.jobservice.service.ApplicationService;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.TransactionsLogService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final String JOB_PENDING_DUE = "PendingDueDateJob";
    private LocalDate localDate = LocalDate.now();

    private final AppProperties appProperties;
    private final TransactionsService transactionsService;
    private final JobLogService jobLogService;
    private final TransactionsLogService transactionsLogService;


    public ApplicationServiceImpl(AppProperties appProperties, TransactionsService transactionsService, JobLogService jobLogService, TransactionsLogService transactionsLogService) {
        this.appProperties = appProperties;
        this.transactionsService = transactionsService;
        this.jobLogService = jobLogService;
        this.transactionsLogService = transactionsLogService;
    }

    @Scheduled(cron = "0 01 * * ?")
    public void generatePendingStatusJob() {
        this.pendingStatusChange();
    }

    @Override
    public List<Transactions> pendingStatusChange() {

        log.info("Enter to pendingStatusChange on date {}", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        PendingStatusJobData pendingStatusJobData = new PendingStatusJobData();

        String cycleDate = createNextCycleDate();
        List<Transactions> transactions = transactionsService.getTransactionsWithProjectedBalance(cycleDate);
        List<String> accounts = transactions.stream().map(Transactions::getChargeAccountId).distinct().collect(Collectors.toList());
        List<Transactions> failedTransactions = new ArrayList<>();
        for (String account : accounts) {
            log.info("get transactions for account {}", account);
            try {
                List<Transactions> transactionsPerAccount = transactions.stream().
                        filter(transactions1 -> transactions1.getChargeAccountId()
                                .equals(account)).
                        sorted(Comparator.comparing(Transactions::getDueDate).reversed()
                                .thenComparing(Transactions::getAmount).reversed())
                        .collect(Collectors.toList());

                transactionsPerAccount = changeStatusByAmount(transactionsPerAccount, failedTransactions, pendingStatusJobData);
                transactionsService.saveAllTransactions(transactionsPerAccount);

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
        createJobLog(pendingStatusJobData, cycleDate);

        return failedTransactions;
    }

    private void createJobLog(PendingStatusJobData pendingStatusJobData, String cycleDate) {
        log.info("create job log with ready for payment : {}  , pendingContribution: {} , with cycle date : {} "
                , pendingStatusJobData.getReadyForPayment(), pendingStatusJobData.getPendingContribution(), cycleDate);

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

    private List<Transactions> changeStatusByAmount(List<Transactions> transactions, List<Transactions> failedTransactions, PendingStatusJobData pendingStatusJobData) {
        double billBalance = 0.0;

        for (Transactions transaction : transactions) {
            try {
                billBalance = billBalance + transaction.getAmount();
                double projectedBalanceWithTrashHold = transaction.getProjected_balance() + appProperties.getTrashHold();
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
                createTransactionLog(transaction);
            }
        }
        return transactions;
    }

    private void createTransactionLog(Transactions transaction) {

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
