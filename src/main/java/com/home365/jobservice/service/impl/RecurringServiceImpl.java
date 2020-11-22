package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.repository.RecurringRepository;
import com.home365.jobservice.service.RecurringService;
import com.home365.jobservice.service.TransactionsService;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringServiceImpl implements RecurringService {

    private final RecurringRepository recurringRepository;
    private final TransactionsService transactionsService;

    public RecurringServiceImpl(RecurringRepository recurringRepository, TransactionsService transactionsService) {
        this.recurringRepository = recurringRepository;
        this.transactionsService = transactionsService;
    }

    @Override
    public List<Recurring> findByActive(boolean isActive) {
        return recurringRepository.findByActive(true);
    }

    @Override
    public JobExecutionResults createTransactionsForRecurringCharges() {
        List<Recurring> activeRecurringChargeList = findByActive(true);

        activeRecurringChargeList.forEach(recurringCharge -> {
            List<Transactions> existingRecurringTransactions = transactionsService.findByRecurringTemplateIdAndDueDateAfter(recurringCharge.getId(), Calendar.getInstance().getTime());
            if (CollectionUtils.isEmpty(existingRecurringTransactions)) {
                Transactions transactions = Transactions.builder()
                        .amount((long) recurringCharge.getAmount())
                        .pmAccountId(recurringCharge.getPmAccountId())
                        .accountingTypeId(recurringCharge.getAccountingTypeId())
                        .amountBeforeDiscount(recurringCharge.getAmountBeforeDiscount() == null ? (long) recurringCharge.getAmount() : (long) recurringCharge.getAmountBeforeDiscount().doubleValue())
                        .billType(recurringCharge.getBillType())
                        .categoryId(recurringCharge.getCategoryId())
                        .memo(recurringCharge.getMemo())
                        .dueDate(new Timestamp(recurringCharge.getDueDate().getTime()))
                        .chargedBy(recurringCharge.getChargedBy())
                        .recurringTemplateId(recurringCharge.getId())
                        .chargeAccountId(recurringCharge.getChargeAccountId())
                        .isDeductible("false")
                        .isRecurring("true")
                        .propertyId(recurringCharge.getPropertyId())
                        .receiveAccountId(recurringCharge.getReceiveAccountId())
                        .status("readyForPayment")
                        .transactionId(UUID.randomUUID().toString())
                        .build();

                List<Transactions> transactionsList = new ArrayList<>();
                transactionsList.add(transactions);
                transactionsService.saveAllTransactions(transactionsList);
            }
        });

        return JobExecutionResults.builder().build();
    }
}
