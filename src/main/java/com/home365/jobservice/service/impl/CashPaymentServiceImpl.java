package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.*;
import com.home365.jobservice.entities.enums.CashPaymentStatus;
import com.home365.jobservice.entities.enums.PaymentMethod;
import com.home365.jobservice.entities.enums.PaymentStatus;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.*;
import com.home365.jobservice.model.enums.BusinessType;
import com.home365.jobservice.model.enums.CreditType;
import com.home365.jobservice.model.enums.TransactionsStatus;
import com.home365.jobservice.model.enums.TransferTo;
import com.home365.jobservice.repository.AccountExtensionRepo;
import com.home365.jobservice.repository.AccountRepository;
import com.home365.jobservice.repository.CashPaymentTrackingRepository;
import com.home365.jobservice.rest.BalanceServiceFeign;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import com.home365.jobservice.service.CashPaymentService;
import com.home365.jobservice.service.PaymentStripeService;
import com.home365.jobservice.service.PaymentsService;
import com.home365.jobservice.service.TransactionsService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CashPaymentServiceImpl implements CashPaymentService {

    @Autowired
    CashPaymentTrackingRepository cashPaymentTrackingRepository;

    @Autowired
    TransactionsService transactionService;

    @Autowired
    PaymentsService paymentsService;

    @Autowired
    AccountExtensionRepo accountExtensionRepo;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PaymentStripeService paymentStripeService;

    @Autowired
    KeyCloakService keyCloakService;

    @Autowired
    BalanceServiceFeign balanceServiceFeign;

    public void handleCashPaymentWebhookResponse(Object request) {
        Map<String, Object> requestMap = (Map<String, Object>) request;
        String eventType = (String) requestMap.get("eventType");
        String mtid = (String) ((Map<String, Object>) requestMap.get("data")).get("mtid");

        Optional<CashPaymentTracking> cashPaymentTrackingOpt = cashPaymentTrackingRepository.findByPaysafeId(mtid);

        if (cashPaymentTrackingOpt.isPresent()) {
            CashPaymentTracking cashPaymentTracking = cashPaymentTrackingOpt.get();
            if (!CashPaymentStatus.PAYMENT_EXPIRED.name().equalsIgnoreCase(eventType)) {
                if(cashPaymentTracking.getSddPayment() == null || !cashPaymentTracking.getSddPayment()) {
                    List<String> transactions = Arrays.asList(cashPaymentTracking.getRelatedTransactions().split(",", -1));
                    transactions.forEach(transaction -> {
                        Optional<Transactions> transactionsOptional = transactionService.findById(transaction);
                        if (transactionsOptional.isPresent()) {
                            Transactions transactionObject = transactionsOptional.get();
                            Payments paymentObj = paymentsService.createAndSavePayments(
                                    transactionObject.getAmount(),
                                    new Timestamp(new Date().getTime()),
                                    PaymentStatus.success,
                                    mtid,
                                    null,
                                    null,
                                    transactionObject.getReceiveAccountId(),
                                    transactionObject.getPmAccountId(),
                                    PaymentMethod.cash
                            );

                            transactionObject.setStatus("paid");
                            transactionObject.setPaymentId(paymentObj.getPaymentId());
                            transactionService.save(transactionObject);
                        }
                    });

                    handleCreditTransactions(cashPaymentTracking.getRelatedTransactions());
                } else {
                    KeycloakResponse token = null;
                    try {
                        token = keyCloakService.getKey();
                        DispositionWrapper dispositionWrapper = new DispositionWrapper();
                        CashPaymentSdd cashPaymentSdd = new CashPaymentSdd();
                        cashPaymentSdd.setCashId(cashPaymentTracking.getId());
                        cashPaymentSdd.setPaySefId(cashPaymentTracking.getPaysafeId());
                        dispositionWrapper.setCashPaymentSdd(cashPaymentSdd);
                        balanceServiceFeign.dispositionTenantPayment(token.getAccess_token(), dispositionWrapper,cashPaymentTracking.getTenantId());
                    } catch (GeneralException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            cashPaymentTracking.setStatus(CashPaymentStatus.valueOf(eventType));
            cashPaymentTrackingRepository.save(cashPaymentTracking);
        }
    }

    private void handleCreditTransactions(String relatedTransactions) {
        List<Transactions> allTransactions = new ArrayList<>();
        String[] transactionIdsArray = relatedTransactions.split(",");
        for (int i = 0; i < transactionIdsArray.length; i++) {
            allTransactions.add(transactionService.findById(transactionIdsArray[i]).get());
        }
        List<Transactions> refundTransactions = new ArrayList<>();
        List<Transactions> chargeTransactions = new ArrayList<>();

        refundTransactions = allTransactions.stream()
                .filter(e -> e.getCreditTransactionType() != null && (e.getCreditTransactionType() == CreditType.credit || e.getCreditTransactionType() == CreditType.refund)).collect(Collectors.toList());

        chargeTransactions = allTransactions.stream()
                .filter(e -> e.getCreditTransactionType() == null || e.getCreditTransactionType() == CreditType.none).collect(Collectors.toList());

        Long refundSum = refundTransactions.stream().mapToLong(value -> Math.abs(value.getAmount())).sum();

        Map<String, List<Transactions>> refundTransactionsByCharge = refundTransactions.parallelStream()
                .collect(Collectors.groupingBy(transactions -> transactions.getChargeAccountId().toLowerCase()));

        Map<String, List<Transactions>> transactionsByReceive = chargeTransactions.parallelStream()
                .collect(Collectors.groupingBy(transactions1 -> transactions1.getReceiveAccountId().toLowerCase()));

        RefundWrapper refundWrapper = new RefundWrapper();
        refundWrapper.setRefundSum(refundSum);

        Map<String, AmountChargeWithRefund> receiveAccountWithAmount = calculateAmountWithRefund(refundTransactionsByCharge, transactionsByReceive, refundWrapper);

        final String pmAccountId = allTransactions.get(0).getPmAccountId();

        Stripe.apiKey = getStripeApiKeyByPmAccountId(pmAccountId);

        payAndUpdateRefundTransactions(Stripe.apiKey, 0, Constants.TRANSACTION_REFUNDED_THE_NUMBER_IS, refundTransactionsByCharge, transactionsByReceive,
                accountExtensionRepo.findById(pmAccountId).get(), receiveAccountWithAmount);

    }

    private Map<String, AmountChargeWithRefund> calculateAmountWithRefund(Map<String, List<Transactions>> refundTransactionsByCharge, Map<String, List<Transactions>> transactionsByReceive, RefundWrapper refundWrapper) {
        log.info("calculate Amount for receive : {}  with refund {}", transactionsByReceive, refundTransactionsByCharge);
        Map<String, AmountChargeWithRefund> receiveAccountWithAmount = new HashMap<>();
        transactionsByReceive.forEach((receive, transactions) -> {
            Long receiveAmount = transactions.stream().mapToLong(Transactions::getAmount).sum();
            AmountChargeWithRefund amountChargeWithRefund = new AmountChargeWithRefund();
            amountChargeWithRefund.setAmountShouldBeCharged(receiveAmount.intValue());
            for (Map.Entry<String, List<Transactions>> entry : refundTransactionsByCharge.entrySet()) {
                String charge = entry.getKey();
                List<Transactions> refundTransactions = entry.getValue();
                if (charge.equalsIgnoreCase(receive)) {
                    Long refundSum = refundTransactions.stream().mapToLong(value -> Math.abs(value.getAmount())).sum();
                    if (receiveAmount >= refundSum) {
                        receiveAmount = receiveAmount - refundSum;
                    } else {
                        refundSum = receiveAmount;
                        receiveAmount = 0L;
                    }
                    amountChargeWithRefund.setActualPay(receiveAmount.intValue());
                    receiveAccountWithAmount.put(receive, amountChargeWithRefund);
                    refundWrapper.setRefundSum(refundWrapper.getRefundSum() - refundSum);
                }
            }
        });

        transactionsByReceive.forEach((receive, transactions) -> {

            for (Map.Entry<String, List<Transactions>> entry : refundTransactionsByCharge.entrySet()) {
                String charge = entry.getKey();
                List<Transactions> refundTransactions = entry.getValue();
                if (!charge.equalsIgnoreCase(receive)) {
                    AmountChargeWithRefund amountChargeWithRefund = new AmountChargeWithRefund();
                    Long receiveAmount = transactions.stream().mapToLong(Transactions::getAmount).sum();
                    amountChargeWithRefund.setAmountShouldBeCharged(receiveAmount.intValue());
                    Long refundSum = refundTransactions.stream().mapToLong(value -> Math.abs(value.getAmount())).sum();
                    if (refundWrapper.getRefundSum() > 0) {
                        if (refundWrapper.getRefundSum() < refundSum)
                            refundSum = refundWrapper.getRefundSum();
                        if (receiveAmount >= refundWrapper.getRefundSum()) {
                            receiveAmount = receiveAmount - refundSum;

                        } else {
                            refundSum = receiveAmount;
                            receiveAmount = 0L;
                        }
                        refundWrapper.setRefundSum(refundWrapper.getRefundSum() - refundSum);
                    }
                    if (!receiveAccountWithAmount.containsKey(receive)) {
                        amountChargeWithRefund.setActualPay(receiveAmount.intValue());
                        receiveAccountWithAmount.put(receive, amountChargeWithRefund);
                    }
                }
            }
        });

        return receiveAccountWithAmount;
    }

    public void payAndUpdateRefundTransactions(String apiKey, long amountFeeInCents,
                                               String description,
                                               Map<String, List<Transactions>> refundedTransactions,
                                               Map<String, List<Transactions>> transactionsByReceive, AccountExtensionBase pmAccount,
                                               Map<String, AmountChargeWithRefund> receiveAccountWithAmount) {
        Map<String, Integer> accounts = new HashMap<>();
        Optional<AccountExtensionBase> accountInsurance = accountExtensionRepo.findDistinctByAccountTypeAndNewManagerId(TransferTo.InsuranceAccount.name(), pmAccount.getAccountId());
        Optional<AccountExtensionBase> accountOperation = accountExtensionRepo.findDistinctByAccountTypeAndNewManagerId(TransferTo.OperationPMAccount.name(), pmAccount.getAccountId());
        accounts.put(accountInsurance.get().getAccountId().toLowerCase(), 0);
        accounts.put(accountOperation.get().getAccountId().toLowerCase(), 0);

        Map<String, Integer> chargeReceivedAccountWithAmount = createMapWithAmount(transactionsByReceive);
        Map<String, Integer> refundChargeAccountWithAmount = createMapWithAmount(refundedTransactions);
        addToAccountsRefundAndCharges(chargeReceivedAccountWithAmount, refundChargeAccountWithAmount, accounts);
        payTransactionAndCreatePaymentForRefund(apiKey, amountFeeInCents, description, accounts, receiveAccountWithAmount, refundedTransactions);

    }

    private Map<String, Integer> createMapWithAmount(Map<String, List<Transactions>> transactionsByReceive) {
        Map<String, Integer> accountWithAmount = new HashMap<>();

        transactionsByReceive.forEach((s, transactions) -> {
            Long amount = transactions.stream().mapToLong(value -> Math.abs(value.getAmount())).sum();
            accountWithAmount.put(s, amount.intValue());
        });
        return accountWithAmount;
    }

    private void addToAccountsRefundAndCharges(Map<String, Integer> chargeReceivedAccountWithAmount,
                                               Map<String, Integer> refundChargeAccountWithAmount, Map<String, Integer> accounts) {
        refundChargeAccountWithAmount.forEach((s, integer) -> {

            if (!accounts.containsKey(s)) {
                accounts.put(s, -integer);
            } else {
                Integer amount = accounts.get(s) - integer;
                accounts.put(s, amount);
            }
        });
        chargeReceivedAccountWithAmount.forEach((s, amountOfCharge) -> {

            if (!accounts.containsKey(s)) {
                accounts.put(s, amountOfCharge);
            } else {
                Integer amount = amountOfCharge - Math.abs(accounts.get(s));
                accounts.put(s, amount);
            }
        });
    }

    private void payTransactionAndCreatePaymentForRefund(String apiKey, long amountFeeInCents,
                                                         String description,
                                                         Map<String, Integer> accounts,
                                                         Map<String, AmountChargeWithRefund> receiveAccountWithAmount
            , Map<String, List<Transactions>> refundedTransactions) {
        List<RefundChargeAndStripe> refunds = new ArrayList<>();

        for (Map.Entry<String, Integer> e : accounts.entrySet()) {
            String key = e.getKey();
            Integer amount = e.getValue();
            if (amount < 0) {
                for (Map.Entry<String, AmountChargeWithRefund> entry : receiveAccountWithAmount.entrySet()) {
                    String s1 = entry.getKey();
                    AmountChargeWithRefund amountChargeWithRefund = entry.getValue();
                    if (!key.equalsIgnoreCase(s1) && !amountChargeWithRefund.getAmountShouldBeCharged()
                            .equals(amountChargeWithRefund.getActualPay())) {
                        int delta = amountChargeWithRefund.getAmountShouldBeCharged() - amountChargeWithRefund.getActualPay();
                        RefundChargeAndStripe refundChargeAndStripe = new RefundChargeAndStripe();
                        refundChargeAndStripe.setChargeAccount(key);
                        refundChargeAndStripe.setReciveAccount(s1);

                        if (delta >= Math.abs(amount)) {
                            refundChargeAndStripe.setAmount(Math.abs(amount));
                            amount = 0;
                        } else {
                            amount = amount + delta;
                            refundChargeAndStripe.setAmount(delta);
                        }
                        refunds.add(refundChargeAndStripe);
                    }
                }

            }
        }

        for (RefundChargeAndStripe refundChargeAndStripe : refunds) {

            TransactionChargeWrapper transactionsWrapper = getTransactionRefundWrapper(refundChargeAndStripe, refundedTransactions);
            if (transactionsWrapper.isHaveAccountsExtensionBase()) {
                if (!transactionsWrapper.getOwnerRefund() && transactionsWrapper.getAmount() > 0) {
                    chargeStripePayment(apiKey, transactionsWrapper, amountFeeInCents, description, false);
                    if (transactionsWrapper.isHaveStripePayment()) {
                        createPayment(transactionsWrapper, refundChargeAndStripe.getReciveAccount());
                        if (transactionsWrapper.isHavePayment()) {
                            updateTransaction(transactionsWrapper);
                        }
                    }
                }

            }
        }
        refundedTransactions.forEach((s, transactions) ->
        {
            transactions.forEach(transactions1 -> transactions1.setStatus(TransactionsStatus.paid.name()));
            transactionService.saveAll(transactions);
        });
    }

    private void createPayment(TransactionChargeWrapper transactionChargeWrapper, String receiveAccount) {
        log.info("START: Create Payment");
        Payments payments = paymentsService.createAndSavePayments(
                transactionChargeWrapper.getAmount(),
                new Timestamp(new Date().getTime()),
                PaymentStatus.success,
                transactionChargeWrapper.getStripePayment().getId(),
                transactionChargeWrapper.getStripePayment().getTransfer(),
                null,
                receiveAccount,
                transactionChargeWrapper.getTransaction().get(0).getPmAccountId(),
                PaymentMethod.transfer
        );
        transactionChargeWrapper.setPayments(payments);
    }

    private void updateTransaction(TransactionChargeWrapper transactionChargeWrapper) {
        log.info("START: Update Transaction");
        transactionChargeWrapper.getTransaction().forEach(transaction -> {
            Payments payments = transactionChargeWrapper.getPayments();
            transaction.setPaymentId(payments.getPaymentId());
            transaction.setStatus(TransactionsStatus.paid.name());

            transactionService.save(transaction);
        });
    }

    private TransactionChargeWrapper getTransactionRefundWrapper(RefundChargeAndStripe refundChargeAndStripe, Map<String, List<Transactions>> refundedTransactions) {
        log.info("START: Get Transaction Charge Wrapper");

        Optional<AccountExtensionBase> receiveAccountExtensionBaseOptional = accountExtensionRepo.findStripeAccountByAccountId(refundChargeAndStripe.getReciveAccount());
        Optional<AccountExtensionBase> chargeAccountExtensionBaseOptional = accountExtensionRepo.findStripeAccountByAccountId(refundChargeAndStripe.getChargeAccount());

        int amount = refundChargeAndStripe.getAmount();
        TransactionChargeWrapper.TransactionChargeWrapperBuilder transactionChargeWrapper = TransactionChargeWrapper.builder();
        if (receiveAccountExtensionBaseOptional.isEmpty() || chargeAccountExtensionBaseOptional.isEmpty()) {
            return transactionChargeWrapper.build();
        }

        List<Transactions> transactions = refundedTransactions.get(refundChargeAndStripe.getChargeAccount());
        return transactionChargeWrapper
                .receiveAccountExtensionBase(receiveAccountExtensionBaseOptional.get())
                .chargeAccountExtensionBase(chargeAccountExtensionBaseOptional.get())
                .transaction(transactions)
                .amount(Math.abs(amount))
                .ownerRefund(chargeAccountExtensionBaseOptional.get().getBusinessType() == BusinessType.RealEstateOwner.getValue())
                .build();

    }

    private void chargeStripePayment(String apiKey, TransactionChargeWrapper transactionChargeWrapper, long amountFeeInCents,
                                     String description, boolean isRefunded) {
        log.info("START: Charge Stripe Payment");
        try {
            Optional<AccountExtension> accountExtensionBaseOptional = accountRepository
                    .findById(transactionChargeWrapper.getReceiveAccountExtensionBase().getAccountId());
            if (accountExtensionBaseOptional.isEmpty()) {
                return;
            }
            String customerAccountId = transactionChargeWrapper.getChargeAccountExtensionBase().getStripeCustomerId();
            if (transactionChargeWrapper.getOwnerRefund() != null && transactionChargeWrapper.getOwnerRefund()) {
                customerAccountId = transactionChargeWrapper.getChargeAccountExtensionBase().getStripeAccountId();
            }
            StripePayment stripePayment = paymentStripeService.createPaymentTypeAndSendForPayment(
                    isRefunded ? transactionChargeWrapper.getReceiveAccountExtensionBase().getStripeRentAccount()
                            : transactionChargeWrapper.getReceiveAccountExtensionBase().getStripeAccountId(),
                    apiKey,
                    customerAccountId,
                    (int) (transactionChargeWrapper.getAmount() + amountFeeInCents),
                    transactionChargeWrapper.getReceiveAccountExtensionBase().getAccountId(),
                    accountExtensionBaseOptional.get().getName(),
                    getDescription(description) + transactionChargeWrapper.getTransaction().parallelStream().mapToInt(Transactions::getTransactionNumberAsInteger).boxed().collect(Collectors.toList()).toString()
            );
            log.info("Response from stripe for charge : {} ", stripePayment);
            transactionChargeWrapper.setStripePayment(stripePayment);
        } catch (StripeException e) {
            recordFailStripePayment(transactionChargeWrapper, e);
        }
    }

    private String getDescription(String description) {
        if (StringUtils.isEmpty(description))
            return Constants.TENANT_CHARGE_WITH_TRANSACTION_NUMBER;
        else
            return description;
    }

    private void recordFailStripePayment(TransactionChargeWrapper transactionChargeWrapper, StripeException e) {
        log.info("START: Record Fail Stripe Payment");
        log.error("Stripe failes with code : {} and massage : {} ", e.getCode(), e.getMessage());
        paymentsService.createAndSavePayments(
                transactionChargeWrapper.getAmount(),
                new Timestamp(new Date().getTime()),
                PaymentStatus.failed,
                e.getRequestId(),
                e.getCode(),
                e.getMessage(),
                transactionChargeWrapper.getChargeAccountExtensionBase().getAccountId(),
                transactionChargeWrapper.getTransaction().get(0).getPmAccountId(),
                PaymentMethod.transfer
        );
        transactionChargeWrapper.getTransaction().forEach(transaction -> {
            transaction.setStatus(TransactionsStatus.paymentFailed.name());
            transactionService.save(transaction);
        });

    }

    private String getStripeApiKeyByPmAccountId(String pmAccountId) {
        String apiKey;
        Optional<AccountExtensionBase> pmAccountExtensionBaseOptional = accountExtensionRepo.findById(pmAccountId);
        if (pmAccountExtensionBaseOptional.isEmpty()) {
            log.error("Account Extension Base is empty, cannot execute stripe checkout");
            return null;
        }
        apiKey = pmAccountExtensionBaseOptional.get().getStripeApiKey();
        return apiKey;
    }
}
