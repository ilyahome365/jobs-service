package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.AccountExtensionBase;
import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.enums.TransactionStatus;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.exception.PropertyNotFoundLocal;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.ChargeWithStripeRequest;
import com.home365.jobservice.model.enums.TransferTo;
import com.home365.jobservice.repository.AccountExtensionRepo;
import com.home365.jobservice.rest.BalanceServiceExternal;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PayBillsService;
import com.home365.jobservice.utils.CodeUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayBillsServiceImpl extends JobExecutorImpl implements PayBillsService {
    private final BalanceServiceExternal balanceServiceExternal;
    private final AccountExtensionRepo accountExtensionRepo;

    protected PayBillsServiceImpl(AppProperties appProperties, MailService mailService,
                                  BalanceServiceExternal balanceServiceExternal, AccountExtensionRepo accountExtensionRepo) {
        super(appProperties, mailService);
        this.balanceServiceExternal = balanceServiceExternal;


        this.accountExtensionRepo = accountExtensionRepo;
    }

    @Override
    public String insurancePayBills(String locationId) throws PropertyNotFoundLocal, GeneralException {
        Optional<AccountExtensionBase> distinctByAccountTypeAndNewManagerId = accountExtensionRepo.findDistinctByAccountTypeAndNewManagerId(TransferTo.InsuranceAccount.name(), locationId);
        if (distinctByAccountTypeAndNewManagerId.isEmpty()) {
            throw new PropertyNotFoundLocal(String.format("Cant find account for : %s ", TransferTo.InsuranceAccount.name()));
        }
        List<Transactions> byChargeAccountIdAndBillType = balanceServiceExternal.findByChargeAccountIdAndBillType(distinctByAccountTypeAndNewManagerId.get().getAccountId(), TransactionType.bill);
        byChargeAccountIdAndBillType = byChargeAccountIdAndBillType.stream().filter(transactions -> Objects.isNull(transactions.getCreditTransactionType()) && transactions.getStatus().equalsIgnoreCase(TransactionStatus.readyForPayment.name())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(byChargeAccountIdAndBillType)) {

            ChargeWithStripeRequest chargeWithStripeRequest = CodeUtils.createChargeWithStripe(byChargeAccountIdAndBillType, null, 0L, false, "Pay insurance ", false);
            balanceServiceExternal.chargeWithStripe(chargeWithStripeRequest);
        }
        return byChargeAccountIdAndBillType.stream().map(Transactions::getTransactionId).collect(Collectors.joining(","));

    }

    @Override
    protected String getJobName() {
        return Constants.INSURANCE_PAY_BILLS;
    }

    @Override
    protected String execute(String locationId) throws Exception {

        return insurancePayBills(locationId);
    }
}
