package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import com.home365.jobservice.model.wrapper.OwnerProjectedBalanceWrapper;

import java.util.List;

public interface PropertyPhaseOutExternal {
    List<Integer> cancelBillsByPropertyIdAndPhaseOutDate(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;
    List<String> cancelAllRecurringByChargeAccount(String accountId) throws GeneralException;
    void createOwnerBillForTenantDebts(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;
    String createTerminationFeeBill(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;
    String createMaterialTransferFee(OwnerBillsWrapper  ownerBillsWrapper) throws GeneralException;
    void moveSecurityDepositToOwnerAccount(String propertyId) throws GeneralException;
    OwnerProjectedBalanceWrapper getOwnerProjectedBalance() throws GeneralException;
}
