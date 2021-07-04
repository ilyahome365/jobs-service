package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.AccountExtensionBase;
import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;
import com.home365.jobservice.entities.enums.TransactionStatus;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.exception.PropertyNotFoundLocal;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.ChargeWithStripeRequest;
import com.home365.jobservice.model.TransactionsDetails;
import com.home365.jobservice.model.enums.BusinessType;
import com.home365.jobservice.model.enums.TransferTo;
import com.home365.jobservice.repository.AccountExtensionRepo;
import com.home365.jobservice.rest.BalanceServiceExternal;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PayBillsService;
import com.home365.jobservice.service.PaymentsService;
import com.home365.jobservice.utils.BusinessActionRequest;
import com.home365.jobservice.utils.CodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PayBillsServiceImpl extends JobExecutorImpl implements PayBillsService {
    private final BalanceServiceExternal balanceServiceExternal;
    private final AccountExtensionRepo accountExtensionRepo;
    private final PaymentsService paymentsService;

    protected PayBillsServiceImpl(AppProperties appProperties, MailService mailService,
                                  BalanceServiceExternal balanceServiceExternal, AccountExtensionRepo accountExtensionRepo, PaymentsService paymentsService) {
        super(appProperties, mailService);
        this.balanceServiceExternal = balanceServiceExternal;


        this.accountExtensionRepo = accountExtensionRepo;
        this.paymentsService = paymentsService;
    }

    public TransactionsDetails payOwnerBills(String locationId) throws GeneralException {
        BusinessActionRequest.setBusinessActionOnRequest(BusinessActionRequest.PAYING_BILLS);
        log.info("Start Owner pay bills for location : {} ", locationId);
        TransactionsDetails transactionsDetails = new TransactionsDetails();
        Timestamp now = new Timestamp(new Date().getTime());
        List<String> statuses = List.of(TransactionStatus.readyForPayment.name());
        List<Transactions> bills = balanceServiceExternal
                .getTransactionsByBusinessTypeAndLocation(BusinessType.RealEstateOwner.getValue(), List.of(locationId), statuses, now);
        List<String> billsReceived = bills.stream().map(Transactions::getReceiveAccountId).collect(Collectors.toList());
        List<AccountExtensionBase> accountsByIds = balanceServiceExternal.getAccountsByIds(billsReceived).stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
        payBillsOrManagementFee(bills, accountsByIds, transactionsDetails);
        payLoans(bills, accountsByIds, transactionsDetails);
        Timestamp checkDate = Timestamp.valueOf(LocalDateTime.now().plusDays(7));
        bills = balanceServiceExternal
                .getTransactionsByBusinessTypeAndLocation(BusinessType.RealEstateOwner.getValue(), List.of(locationId), statuses, checkDate);
        payBillsChecks(bills, accountsByIds, transactionsDetails);
        return transactionsDetails;
    }

    private void payBillsChecks(List<Transactions> bills, List<AccountExtensionBase> accountsByIds, TransactionsDetails transactionsDetails) throws GeneralException {
        List<AccountExtensionBase> accounts = accountsByIds.parallelStream()
                .filter(accountExtensionBase ->
                        Objects.nonNull(accountExtensionBase.getPayeeMethod())
                                && accountExtensionBase.getPayeeMethod().equals(PaymentMethod.check.ordinal())
                                && accountExtensionBase.getBusinessType() != BusinessType.Tenant.getValue())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(accounts)) {
            TreeMap<String, AccountExtensionBase> accountsTree = accounts.stream().collect(Collectors.toMap(AccountExtensionBase::getAccountId,
                    Function.identity(), (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
            List<Transactions> transactionsList = bills.parallelStream()
                    .filter(transactions -> (transactions.getBillType().equalsIgnoreCase(TransactionType.bill.name())
                            && accountsTree.containsKey(transactions.getReceiveAccountId())))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(transactionsList))
                payTransactionsByChecks(transactionsList, transactionsDetails);
        }
    }

    private void payTransactionsByChecks(List<Transactions> transactionsList, TransactionsDetails transactionsDetails) throws GeneralException {
        List<String> checksIds = transactionsList.stream().map(Transactions::getTransactionId).collect(Collectors.toList());
        balanceServiceExternal.payCheckBills(checksIds);
        transactionsDetails.setTransactionNumberPaid(checksIds);
    }

    private void payLoans(List<Transactions> bills, List<AccountExtensionBase> accountsByIds, TransactionsDetails transactionsDetails) throws GeneralException {
        log.info("Start pay loans ");
        List<AccountExtensionBase> accounts = accountsByIds.parallelStream()
                .filter(accountExtensionBase -> Objects.nonNull(accountExtensionBase.getPayeeMethod()) && accountExtensionBase.getPayeeMethod().equals(PaymentMethod.transfer.ordinal()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(accounts)) {
            TreeMap<String, AccountExtensionBase> accountsTree = accounts.stream().collect(Collectors.toMap(AccountExtensionBase::getAccountId, Function.identity(),
                    (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
            List<Transactions> transactionsList = bills.parallelStream()
                    .filter(transactions -> (transactions.getBillType().equalsIgnoreCase(TransactionType.loan.name())
                            && accountsTree.containsKey(transactions.getReceiveAccountId())))
                    .collect(Collectors.toList());
            payTransactionsByStripe(transactionsDetails, transactionsList);
        }
        accounts = accountsByIds.parallelStream().filter(accountExtensionBase -> Objects.nonNull(accountExtensionBase.getPayeeMethod())
                && (accountExtensionBase.getPayeeMethod()
                .equals(PaymentMethod.other.ordinal()) || accountExtensionBase.getPayeeMethod()
                .equals(PaymentMethod.noPaymentMethod.ordinal()))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(accounts)) {
            TreeMap<String, AccountExtensionBase> accountsTree = accounts.stream().collect(Collectors.toMap(AccountExtensionBase::getAccountId, Function.identity()
                    , (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
            List<Transactions> billsWhoReceivedOther = bills.parallelStream()
                    .filter(transactions -> transactions.getBillType().equalsIgnoreCase(TransactionType.loan.name())
                            && accountsTree.containsKey(transactions.getReceiveAccountId())).collect(Collectors.toList());
            updateTransactionsOther(billsWhoReceivedOther, transactionsDetails);
        }
    }

    private void payBillsOrManagementFee(List<Transactions> bills, List<AccountExtensionBase> accountsByIds, TransactionsDetails transactionsDetails) throws GeneralException {
        log.info("Start pay bills or management fee ");
        List<AccountExtensionBase> accounts = accountsByIds.parallelStream()
                .filter(accountExtensionBase -> Objects.nonNull(accountExtensionBase.getPayeeMethod()) && accountExtensionBase.getPayeeMethod().equals(PaymentMethod.transfer.ordinal()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(accounts)) {
            TreeMap<String, AccountExtensionBase> finalAccountTree = accounts.stream().collect(Collectors.toMap(AccountExtensionBase::getAccountId,
                    Function.identity(), (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
            List<Transactions> transactionsList = bills.parallelStream().filter(transactions -> (transactions.getBillType().equalsIgnoreCase(TransactionType.bill.name())
                    || transactions.getBillType().equalsIgnoreCase(TransactionType.managementFee.name()))
                    && finalAccountTree.containsKey(transactions.getReceiveAccountId()))
                    .collect(Collectors.toList());
                payTransactionsByStripe(transactionsDetails, transactionsList);
        }
        accounts = accountsByIds.parallelStream().filter(accountExtensionBase -> Objects.nonNull(accountExtensionBase.getPayeeMethod())
                && (accountExtensionBase.getPayeeMethod()
                .equals(PaymentMethod.other.ordinal()) || accountExtensionBase.getPayeeMethod()
                .equals(PaymentMethod.noPaymentMethod.ordinal())))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(accounts)) {
            TreeMap<String, AccountExtensionBase> accountsTree = accounts.stream().collect(Collectors.toMap(AccountExtensionBase::getAccountId,
                    Function.identity(), (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
            List<Transactions> billsWhoReceivedOther = bills.stream().filter(transactions -> (transactions.getBillType().equalsIgnoreCase(TransactionType.bill.name())
                    || transactions.getBillType().equalsIgnoreCase(TransactionType.managementFee.name()))
                    && accountsTree.containsKey(transactions.getReceiveAccountId())).collect(Collectors.toList());
            updateTransactionsOther(billsWhoReceivedOther, transactionsDetails);
        }
    }

    private void payTransactionsByStripe(TransactionsDetails transactionsDetails, List<Transactions> transactionsList)  {

        if (!CollectionUtils.isEmpty(transactionsList)) {
            List<Transactions> wpChargeTransactions = transactionsList.stream().filter(e -> e.getChargeAccountId().equalsIgnoreCase("6791E98E-10CD-4B4D-8C6C-FCFD7F4010CD")).collect(Collectors.toList());
            List<Transactions> notWPChargeTransactions = transactionsList.stream().filter(e -> !e.getChargeAccountId().equalsIgnoreCase("6791E98E-10CD-4B4D-8C6C-FCFD7F4010CD")).collect(Collectors.toList());
                ChargeWithStripeRequest chargeWithStripeRequest = new ChargeWithStripeRequest();
                List<String> transactionsIds = notWPChargeTransactions.stream().map(Transactions::getTransactionId).collect(Collectors.toList());
                chargeWithStripeRequest.setCharges(transactionsIds);
                chargeWithStripeRequest.setIsRefunded(false);
                chargeWithStripeRequest.setSendMailFlag(false);
                chargeWithStripeRequest.setDescription("Pay Bills or managements fee for owners");
                try {
                    balanceServiceExternal.chargeWithStripe(chargeWithStripeRequest, " paying bill job");
                } catch (GeneralException e) {
                    log.error("ERROR from BALANCE SERVICE : {}" , e.getMessage());
                }
                transactionsDetails.setTransactionNumberPaid(transactionsIds);



        }
    }

    private void updateTransactionsOther(List<Transactions> billsWhoReceivedOther, TransactionsDetails transactionsDetails) throws GeneralException {
        if (!CollectionUtils.isEmpty(billsWhoReceivedOther)) {
            billsWhoReceivedOther.forEach(transactions -> {
                Payments received_account_other = paymentsService.createAndSavePayments(transactions.getAmount(), new Timestamp(new Date().getTime()),
                        PaymentStatus.failed, null, null,
                        "Received account : has no Payee method", transactions.getReceiveAccountId(), transactions.getPmAccountId(), PaymentMethod.other
                );
                transactions.setStatus(TransactionStatus.paymentFailed.name());
                transactions.setPaymentId(received_account_other.getPaymentId());

            });
            balanceServiceExternal.saveAllTransactions(billsWhoReceivedOther);
            transactionsDetails.setTransactionsNumberDidntPaid(billsWhoReceivedOther.stream().map(Transactions::getTransactionId).collect(Collectors.toList()));
        }
    }

    @Override
    public String insurancePayBills(String locationId) throws PropertyNotFoundLocal, GeneralException {
        log.info("Start Insurance pay bills for location : {} ", locationId);
        Optional<AccountExtensionBase> distinctByAccountTypeAndNewManagerId = accountExtensionRepo.findDistinctByAccountTypeAndNewManagerId(TransferTo.InsuranceAccount.name(), locationId);
        if (distinctByAccountTypeAndNewManagerId.isEmpty()) {
            throw new PropertyNotFoundLocal(String.format("Cant find account for : %s ", TransferTo.InsuranceAccount.name()));
        }
        List<Transactions> byChargeAccountIdAndBillType = balanceServiceExternal.findByChargeAccountIdAndBillType(distinctByAccountTypeAndNewManagerId.get().getAccountId(), TransactionType.bill);
        byChargeAccountIdAndBillType = byChargeAccountIdAndBillType.stream().filter(transactions -> Objects.isNull(transactions.getCreditTransactionType()) && transactions.getStatus().equalsIgnoreCase(TransactionStatus.readyForPayment.name())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(byChargeAccountIdAndBillType)) {
            ChargeWithStripeRequest chargeWithStripeRequest = CodeUtils.createChargeWithStripe(byChargeAccountIdAndBillType, null, 0L, false, "Pay insurance ", false);
            balanceServiceExternal.chargeWithStripe(chargeWithStripeRequest, " paying insurance bill job" );
        }


        return byChargeAccountIdAndBillType.stream().map(Transactions::getTransactionId).collect(Collectors.joining(","));

    }

    @Override
    protected String getJobName() {
        return Constants.INSURANCE_PAY_BILLS;
    }

    @Override
    protected String execute(String locationId) throws Exception {

        String insurancePayBills = insurancePayBills(locationId);
        TransactionsDetails transactionsDetails = payOwnerBills(locationId);
        payTenantBills(locationId, transactionsDetails);
        return insurancePayBills + transactionsDetails.toString();
    }

    private void payTenantBills(String locationId, TransactionsDetails transactionsDetails) throws GeneralException {
        log.info("Start pay tenant bills");
        Timestamp now = new Timestamp(new Date().getTime());
        List<String> statuses = List.of(TransactionStatus.readyForPayment.name());
        List<Transactions> bills = balanceServiceExternal
                .getTransactionsByBusinessTypeAndLocation(BusinessType.RealEstateOwner.getValue(), List.of(locationId), statuses, now);
        List<String> billsReceived = bills.stream().map(Transactions::getReceiveAccountId).collect(Collectors.toList());
        TreeMap<String, AccountExtensionBase> accountsByIds = balanceServiceExternal.getAccountsByIds(billsReceived).stream()
                .filter(Objects::nonNull).filter(accountExtensionBase -> accountExtensionBase.getBusinessType() == BusinessType.Tenant.getValue())
                .collect(Collectors.toMap(AccountExtensionBase::getAccountId,
                        Function.identity(), (v1, v2) -> v1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));

        List<Transactions> transactionsList = bills.stream().filter(transactions -> transactions.getBillType()
                .equalsIgnoreCase(TransactionType.bill.name()) && accountsByIds.containsKey(transactions.getReceiveAccountId()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(transactionsList)) {
            payTransactionsByChecks(transactionsList, transactionsDetails);

        }
    }
}
