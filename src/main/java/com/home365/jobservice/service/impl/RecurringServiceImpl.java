package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import com.home365.jobservice.entities.LocationRules;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.repository.RecurringRepository;
import com.home365.jobservice.service.LocationRulesService;
import com.home365.jobservice.service.RecurringService;
import com.home365.jobservice.service.TransactionsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecurringServiceImpl implements RecurringService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final RecurringRepository recurringRepository;
    private final TransactionsService transactionsService;
    private final LocationRulesService locationRulesService;

    public RecurringServiceImpl(RecurringRepository recurringRepository, TransactionsService transactionsService,
                                LocationRulesService locationRulesService) {
        this.recurringRepository = recurringRepository;
        this.transactionsService = transactionsService;
        this.locationRulesService = locationRulesService;
    }

    @Override
    public List<Recurring> findByActive(boolean isActive) {
        return recurringRepository.findByActive(true);
    }

    @Override
    public Optional<Recurring> findById(String recurringId) {
        return Optional.empty();
    }

    @Override
    public Recurring save(Recurring recurring) {
        return null;
    }

    @Override
    public JobExecutionResults createTransactionsForRecurringCharges() {
        List<Recurring> activeRecurringChargeList = findByActive(true);

        String lvPmAccountId = "F90E128A-CD00-4DF7-B0D0-0F40F80D623A";

        Optional<LocationRules> locationRules = locationRulesService.findLocationRulesById(lvPmAccountId);

        Map<String, String> rules = new HashMap<>();

        try {
            rules = mapper.readValue(locationRules.get().getRules(), HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        final Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        int daysToCreateRecurring = Integer.parseInt(rules.get("days_to_create_recurring"));

        calendar.add(Calendar.DAY_OF_MONTH, daysToCreateRecurring);
        Date dateToCreateRecurring = calendar.getTime();

        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date nextDueDate = calendar.getTime();

        activeRecurringChargeList.forEach(recurringCharge -> {
            List<Transactions> existingRecurringTransactions = transactionsService.findByRecurringTemplateId(recurringCharge.getId());
            if (CollectionUtils.isEmpty(existingRecurringTransactions)) {
                final long relativeAmount = getRelativeAmount(recurringCharge.getAmount());
                Transactions transactions = Transactions.builder()
                        .amount(relativeAmount)
                        .pmAccountId(recurringCharge.getPmAccountId())
                        .accountingTypeId(recurringCharge.getAccountingTypeId())
                        .amountBeforeDiscount(relativeAmount)
                        .billType(recurringCharge.getBillType())
                        .categoryId(recurringCharge.getCategoryId())
                        .memo(recurringCharge.getMemo())
                        .dueDate(new Timestamp(now.getTime()))
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
            } else if (dateToCreateRecurring.equals(nextDueDate)) {
                existingRecurringTransactions = existingRecurringTransactions.stream().filter(transactions -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    LocalDate localDueDate = LocalDate.parse(sdf.format(transactions.getDueDate()));
                    LocalDate localNextDueDate = LocalDate.parse(sdf.format(nextDueDate.getTime()));
                    return localDueDate.equals(localNextDueDate);
                }).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(existingRecurringTransactions)) {
                    Transactions transactions = Transactions.builder()
                            .amount((long) recurringCharge.getAmount())
                            .pmAccountId(recurringCharge.getPmAccountId())
                            .accountingTypeId(recurringCharge.getAccountingTypeId())
                            .amountBeforeDiscount(recurringCharge.getAmountBeforeDiscount() == null ? (long) recurringCharge.getAmount() : (long) recurringCharge.getAmountBeforeDiscount().doubleValue())
                            .billType(recurringCharge.getBillType())
                            .categoryId(recurringCharge.getCategoryId())
                            .memo(recurringCharge.getMemo())
                            .dueDate(new Timestamp(nextDueDate.getTime()))
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
            }
        });

        return JobExecutionResults.builder().build();
    }

    private long getRelativeAmount(double amount) {
        Calendar calendar = Calendar.getInstance();
        double lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        double currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        double daysLeft = lastDay - currentDay;

        return (long) (amount * (daysLeft / lastDay));
    }

    @Override
    public List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate) {
        return recurringRepository.findAllForLeaseNotification(startDate, endDate);
    }

    @Override
    public List<IPropertyLeaseInformationProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds) {
        return recurringRepository.getRecurrentPropertyAndTenantByRecurringIds(recurringIds);
    }
}
