package com.home365.jobservice.rest.impl;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.AccountBalance;
import com.home365.jobservice.model.ChargeWithStripeRequest;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.wrapper.CancelChargeWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import com.home365.jobservice.model.wrapper.OwnerProjectedBalanceWrapper;
import com.home365.jobservice.rest.BalanceServiceExternal;
import com.home365.jobservice.rest.BalanceServiceFeign;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BalanceServiceExternalImpl implements BalanceServiceExternal {
    private final KeyCloakService keyCloakService;
    private final BalanceServiceFeign balanceServiceFeign;

    public BalanceServiceExternalImpl(KeyCloakService keyCloakService, BalanceServiceFeign balanceServiceFeign) {
        this.keyCloakService = keyCloakService;
        this.balanceServiceFeign = balanceServiceFeign;
    }

    @Override
    public List<Integer> cancelBillsByPropertyIdAndPhaseOutDate(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.cancelBillsByPropertyAndDueDate(token.getAccess_token(), propertyPhasingOutWrapper);
    }

    @Override
    public List<String> cancelAllRecurringByChargeAccount(String accountId) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        CancelChargeWrapper cancelChargeWrapper = new CancelChargeWrapper();
        cancelChargeWrapper.setAccountId(accountId);
        return balanceServiceFeign.cancelAllRecurringByAccount(token.getAccess_token(), cancelChargeWrapper);
    }

    @Override
    public void createOwnerBillForTenantDebts(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        balanceServiceFeign.createOwnerBillForTenantDebts(token.getAccess_token(), ownerBillsWrapper);
    }

    @Override
    public String createTerminationFeeBill(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.createTerminationFeeByProperty(token.getAccess_token(), ownerBillsWrapper);
    }

    @Override
    public String createMaterialTransferFee(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.createMaterialTransferFee(token.getAccess_token(), ownerBillsWrapper);
    }

    @Override
    public void moveSecurityDepositToOwnerAccount(String propertyId) throws GeneralException {
        log.info("Start move security deposit to owner account");
        KeycloakResponse token = keyCloakService.getKey();
        balanceServiceFeign.moveSecurityDepositToOwnerAccount(token.getAccess_token(), propertyId);
    }

    @Override
    public OwnerProjectedBalanceWrapper getAllOwnersProjectedBalance() throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.getAllOwnersProjectedBalance(token.getAccess_token());
    }

    @Override
    public AccountBalance getOwnerProjectedBalance(String accountId) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();

        return balanceServiceFeign.getOwnerProjectedBalance(token.getAccess_token(), accountId, true);
    }

    @Override
    public List<Transactions> findByChargeAccountIdAndBillType(String chargeAccountId, TransactionType transactionType) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.getTransactionsByChargeAccountAndBillType(token.getAccess_token(), chargeAccountId, transactionType);
    }

    @Override
    public void chargeWithStripe(ChargeWithStripeRequest chargeWithStripeRequest) throws GeneralException {
        balanceServiceFeign.chargeWithStripe(keyCloakService.getKey().getAccess_token(), chargeWithStripeRequest);
    }
}
