package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.wrapper.CancelChargeWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface BalanceServiceFeign {

    @RequestLine("POST /bills/cancel-bills-by-property-and-dueDate")
    @Headers({"Authorization: Bearer {token}","Content-Type: application/json"})
    List<Integer> cancelBillsByPropertyAndDueDate(@Param("token") String token,
                                                  PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;

    @RequestLine("POST /charges/cancel-all-recurring")
    @Headers({"Authorization: Bearer {token}","Content-Type: application/json"})
    List<String> cancelAllRecurringByAccount(@Param("token") String token,
                                              CancelChargeWrapper cancelChargeWrapper) throws GeneralException;

    @RequestLine("POST /bills/create-owner-bill-for-tenant-debts")
    @Headers({"Authorization: Bearer {token}","Content-Type: application/json"})
    void createOwnerBillForTenantDebts(@Param("token") String token,
                                             OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;


}
