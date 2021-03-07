package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.*;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecurringServiceImpl extends JobExecutorImpl implements RecurringService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final RecurringRepository recurringRepository;
    private final TransactionsService transactionsService;
    private final LocationRulesService locationRulesService;
    private final TypeCategoryRepository typeCategoryRepository;


    @PersistenceContext
    private EntityManager entityManager;


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

        int activeRecurringChargeListSize = activeRecurringChargeList.size();

        StringBuffer responseStr = new StringBuffer();
        responseStr.append("Number of recurring charges: " + activeRecurringChargeList.size() + "\n");
        responseStr.append("Number of installments charges: " + installmentsRecurringChargeList.size() + "\n");

        lvPmAccountId = "F90E128A-CD00-4DF7-B0D0-0F40F80D623A";

        Optional<LocationRules> locationRules = locationRulesService.findLocationRulesById(lvPmAccountId);
        Rules rules = null;

        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            rules = mapper.readValue(locationRules.get().getRules(), Rules.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            rules = new Rules();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        final Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        int dayInMonthToCreateRecurring = rules.getDayInmMonthToCreateRecurring();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date nextDueDate = calendar.getTime();

        Date finalNow = now;
        AtomicInteger counter = new AtomicInteger();
        activeRecurringChargeList.forEach(recurringCharge -> {

            calendar.setTime(finalNow);
            List<IPropertyLeaseInformation> leaseList = recurringRepository.getLeaseDatesByLeaseId(recurringCharge.getLeaseId());
            if (CollectionUtils.isEmpty(leaseList) || leaseList.size() != 1) {
                log.error("Cannot create transactions for recurring charges of propertyId {} since no active lease or more than 1 active lease has been found", recurringCharge.getPropertyId());
                return;
            }

            Date leaseStartDate = leaseList.get(0).getStartDate();
            Date moveOutDate = leaseList.get(0).getMoveOutDate();

            List<Transactions> existingRecurringTransactions = transactionsService.findByRecurringTemplateId(recurringCharge.getId());
//            if (CollectionUtils.isEmpty(existingRecurringTransactions) && !"Imported from Buildium".equals(recurringCharge.getMemo())) {
//                log.error("Cannot create transactions for recurring charges of propertyId {} since no first charge have been found", recurringCharge.getPropertyId());
//                return;
//            } else
            if (dayInMonthToCreateRecurring == calendar.get(Calendar.DAY_OF_MONTH) && (moveOutDate == null || moveOutDate.after(nextDueDate)) && (leaseStartDate == null || leaseStartDate.before(nextDueDate))) {
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
                            log.warn("No need to create a transaction for recurring {} since first transaction (due date :{}, amount: {}) handle the charge", recurringCharge.getId(), firstTransaction.getDueDate(), firstTransaction.getAmount());
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
                } else {
                    log.warn("No need to create a transaction for recurring {} since already has a transaction for month {}", recurringCharge.getId(), nextDueDate);
                }
            } else {
                log.warn("No need to create a transaction for recurring {} due to move out date or lease end date", recurringCharge.getId());
            }
            counter.getAndIncrement();
            log.info("Remain: {}", activeRecurringChargeListSize - counter.get());
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

        DateFormat descriptionSDF = new SimpleDateFormat("MMM yyyy");
        String odDueDateDescriptionStr = descriptionSDF.format(dueDate);

        String category = typeCategoryRepository.getCategoryNameByID(recurringCharge.getCategoryId());

        Transactions transaction = Transactions.builder()
                .amount((long) recurringCharge.getAmount())
                .pmAccountId(recurringCharge.getPmAccountId())
                .accountingTypeId(recurringCharge.getAccountingTypeId())
                .accountingName(typeCategoryRepository.getTypeNameByID(recurringCharge.getAccountingTypeId()))
                .amountBeforeDiscount(recurringCharge.getAmountBeforeDiscount() == null ? (long) recurringCharge.getAmount() : (long) recurringCharge.getAmountBeforeDiscount().doubleValue())
                .billType(recurringCharge.getBillType())
                .categoryId(recurringCharge.getCategoryId())
                .categoryName(category)
                .memo(category + " for " + odDueDateDescriptionStr)
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
                .statementType(StringUtils.isEmpty(recurringCharge.getStatementType()) ? null : recurringCharge.getStatementType().toLowerCase())
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

    @Override
    public IAuditableEntity findByIdAudit(IAuditableEntity newEntity) {
        entityManager.detach(newEntity);
        return this.findById(newEntity.idOfEntity()).orElse(null);
    }
}
