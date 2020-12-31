package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.IPropertyLeaseInformation;
import com.home365.jobservice.entities.LocationRules;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.repository.RecurringRepository;
import com.home365.jobservice.repository.TypeCategoryRepository;
import com.home365.jobservice.service.LocationRulesService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.RecurringService;
import com.home365.jobservice.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecurringServiceImpl extends JobExecutorImpl implements RecurringService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final RecurringRepository recurringRepository;
    private final TransactionsService transactionsService;
    private final LocationRulesService locationRulesService;
    private final TypeCategoryRepository typeCategoryRepository;

    public RecurringServiceImpl(AppProperties appProperties, MailService mailService, RecurringRepository recurringRepository, TransactionsService transactionsService,
                                LocationRulesService locationRulesService, TypeCategoryRepository typeCategoryRepository) {
        super(appProperties, mailService);
        this.recurringRepository = recurringRepository;
        this.transactionsService = transactionsService;
        this.locationRulesService = locationRulesService;
        this.typeCategoryRepository = typeCategoryRepository;
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
        return recurringRepository.save(recurring);
    }

    @Override
    public String createTransactionsForRecurringCharges(String lvPmAccountId) {
        List<Recurring> activeRecurringChargeList = findByActive(true);

        List<Recurring> installmentsRecurringChargeList = activeRecurringChargeList.stream().filter(recurring -> recurring.getNumOfInstallments() > 0).collect(Collectors.toList());
        activeRecurringChargeList = activeRecurringChargeList.stream().filter(recurring -> recurring.getNumOfInstallments() == 0).collect(Collectors.toList());

        StringBuffer responseStr = new StringBuffer();
        responseStr.append("Number of recurring charges: "+ activeRecurringChargeList.size()+ "\n");
        responseStr.append("Number of installments charges: "+ installmentsRecurringChargeList.size()+ "\n");

        lvPmAccountId = "F90E128A-CD00-4DF7-B0D0-0F40F80D623A";

        Optional<LocationRules> locationRules = locationRulesService.findLocationRulesById(lvPmAccountId);

        Map<String, String> rules = new HashMap<>();

        try {
            rules = mapper.readValue(locationRules.get().getRules(), HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        final Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        int dayInMonthToCreateRecurring = Integer.parseInt(rules.get("day_in_month_to_create_recurring"));
        String logicalDateStr = rules.get("logical_date");

        try {
            if (!StringUtils.isEmpty(logicalDateStr)) {
                now = sdf.parse(logicalDateStr);
            }
        } catch (ParseException e) {
            log.error("Cannot set logical date");
        }

        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date nextDueDate = calendar.getTime();

        Date finalNow = now;
        activeRecurringChargeList.forEach(recurringCharge -> {

            calendar.setTime(finalNow);
            List<IPropertyLeaseInformation> leaseList = recurringRepository.getLeaseDatesByLeaseId(recurringCharge.getLeaseId());
            if (CollectionUtils.isEmpty(leaseList) || leaseList.size() != 1) {
                log.error("Cannot create transactions for recurring charges of propertyId {} since no active lease or more than 1 active lease has been found", recurringCharge.getPropertyId());
                return;
            }

            Date leaseEndDate = leaseList.get(0).getEndDate();
            Date moveOutDate = leaseList.get(0).getMoveOutDate();

            List<Transactions> existingRecurringTransactions = transactionsService.findByRecurringTemplateId(recurringCharge.getId());
            if (CollectionUtils.isEmpty(existingRecurringTransactions) && !"Imported from Buildium".equals(recurringCharge.getMemo())) {
                log.error("Cannot create transactions for recurring charges of propertyId {} since no first charge have been found", recurringCharge.getPropertyId());
                return;
            } else if (dayInMonthToCreateRecurring == calendar.get(Calendar.DAY_OF_MONTH) && moveOutDate == null) {
                if (existingRecurringTransactions.size() == 1) {
                    Transactions firstTransaction = existingRecurringTransactions.get(0);
                    Date firstDueDate = firstTransaction.getDueDate();
                    double firstAmount = firstTransaction.getAmount();

                    if (firstAmount > recurringCharge.getAmount()) {
                        calendar.setTime(firstDueDate);
                        calendar.add(Calendar.MONTH, 1);

                        int firstDueDateMonthInc = calendar.get(Calendar.MONTH);
                        calendar.setTime(nextDueDate);
                        if (firstDueDateMonthInc == calendar.get(Calendar.MONTH)) {
                            return;
                        }
                    }
                }
                existingRecurringTransactions = existingRecurringTransactions.stream().filter(transactions -> {

                    LocalDate localDueDate = LocalDate.parse(sdf.format(transactions.getDueDate()));
                    LocalDate localNextDueDate = LocalDate.parse(sdf.format(nextDueDate.getTime()));
                    return localDueDate.equals(localNextDueDate);
                }).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(existingRecurringTransactions)) {
                    Transactions transactions = createTransaction(recurringCharge, nextDueDate);
                    List<Transactions> transactionsList = new ArrayList<>();
                    transactionsList.add(transactions);
                    transactionsService.saveAllTransactions(transactionsList);
                }
            }
        });

        handleInstallmentsCharges(installmentsRecurringChargeList, nextDueDate);

        return responseStr.toString();
    }

    private void handleInstallmentsCharges(List<Recurring> installmentsRecurringChargeList, Date nextDueDate) {
        for (Recurring recurringCharge : installmentsRecurringChargeList) {
            int remainInstallments = recurringCharge.getRemainInstallments();
            if (remainInstallments == 0) {
                return;
            }
            String propertyId = recurringCharge.getPropertyId();
            List<Transactions> rentTransactions = transactionsService.findTenantRentTransactionsByPropertyId(propertyId);

            rentTransactions = rentTransactions.stream().filter(transaction -> {
                LocalDate transactionDueDate = transaction.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate nextLocalDueDate = nextDueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return nextLocalDueDate.isEqual(transactionDueDate);
            }).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(rentTransactions) && rentTransactions.size() == 1) {
                Transactions transaction = createTransaction(recurringCharge, nextDueDate);
                transaction.setReferenceTransactionId(rentTransactions.get(0).getTransactionId());

                recurringCharge.setRemainInstallments(remainInstallments - 1);
                save(recurringCharge);
                transactionsService.save(transaction);
            }
        }
    }

    @Override
    public List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate) {
        return recurringRepository.findAllForLeaseNotification(startDate, endDate);
    }

    @Override
    public List<IPropertyLeaseInformationProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds) {
        return recurringRepository.getRecurrentPropertyAndTenantByRecurringIds(recurringIds);
    }

    @Override
    public List<IPropertyLeaseInformation> getLeaseDatesByLeaseId(@Param("leaseId") String leaseId) {
        return recurringRepository.getLeaseDatesByLeaseId(leaseId);
    }

    private Transactions createTransaction(Recurring recurringCharge, Date dueDate) {
        Transactions transaction = Transactions.builder()
                .amount((long) recurringCharge.getAmount())
                .pmAccountId(recurringCharge.getPmAccountId())
                .accountingTypeId(recurringCharge.getAccountingTypeId())
                .accountingName(typeCategoryRepository.getTypeNameByID(recurringCharge.getAccountingTypeId()))
                .amountBeforeDiscount(recurringCharge.getAmountBeforeDiscount() == null ? (long) recurringCharge.getAmount() : (long) recurringCharge.getAmountBeforeDiscount().doubleValue())
                .billType(recurringCharge.getBillType())
                .categoryId(recurringCharge.getCategoryId())
                .categoryName(typeCategoryRepository.getCategoryNameByID(recurringCharge.getCategoryId()))
                .memo(recurringCharge.getMemo())
                .dueDate(new Timestamp(dueDate.getTime()))
                .chargedBy(recurringCharge.getChargedBy())
                .recurringTemplateId(recurringCharge.getId())
                .chargeAccountId(recurringCharge.getChargeAccountId())
                .isDeductible("false")
                .isRecurring("true")
                .propertyId(recurringCharge.getPropertyId())
                .receiveAccountId(recurringCharge.getReceiveAccountId())
                .status("readyForPayment")
                .transactionId(UUID.randomUUID().toString())
                .transactionType("Charge")
                .statementType(recurringCharge.getStatementType().toLowerCase())
                .build();

        return transaction;
    }

    @Override
    protected String getJobName() {
        return "create-recurring-transactions-job";
    }

    @Override
    public String execute(String locationId) {
        return createTransactionsForRecurringCharges(locationId);
    }
}
