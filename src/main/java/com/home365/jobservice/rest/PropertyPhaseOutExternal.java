package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;

import java.util.List;

public interface PropertyPhaseOutExternal {
    List<Integer> cancelBillsByPropertyIdAndPhaseOutDate(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;
    List<String> cancelAllRecurringByChargeAccount(String accountId) throws GeneralException;
    void createOwnerBillForTenantDebts(OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;
}
