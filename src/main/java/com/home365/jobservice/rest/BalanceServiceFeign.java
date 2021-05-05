package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.AccountBalance;
import com.home365.jobservice.model.DispositionWrapper;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.wrapper.CancelChargeWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import com.home365.jobservice.model.wrapper.OwnerProjectedBalanceWrapper;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface BalanceServiceFeign {

    @RequestLine("POST /finance/bills/cancel-bills-by-property-and-dueDate")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<Integer> cancelBillsByPropertyAndDueDate(@Param("token") String token,
                                                  PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;

    @RequestLine("POST /finance/charges/cancel-all-recurring")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<String> cancelAllRecurringByAccount(@Param("token") String token,
                                             CancelChargeWrapper cancelChargeWrapper) throws GeneralException;

    @RequestLine("POST /finance/bills/create-owner-bill-for-tenant-debts")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void createOwnerBillForTenantDebts(@Param("token") String token,
                                       OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("POST /finance/bills/create-Termination-Fee")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String createTerminationFeeByProperty(@Param("token") String token,
                                          OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("POST /finance/bills/create-material-transfer-fee")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String createMaterialTransferFee(@Param("token") String token,
                                     OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("GET /finance/move-security-deposit-to-owner-account?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String moveSecurityDepositToOwnerAccount(@Param("token") String token,
                                             @Param("propertyId") String propertyId) throws GeneralException;

    @RequestLine("GET /finance/get-owner-projected-balance")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    OwnerProjectedBalanceWrapper getAllOwnersProjectedBalance(@Param("token") String token) throws GeneralException;


    @RequestLine("GET /finance/move-late-fee-to-home365?chargesIds={chargesIds}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void moveLateFeeToPM(@Param("token") String token ,@Param("chargesIds") List<String>chargesIds) throws GeneralException;

    @RequestLine("GET /finance/account/{accountId}/balance?isProjectedBalance={isProjectedBalance}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    AccountBalance getOwnerProjectedBalance(@Param("token") String token, @Param("accountId") String accountId, @Param("isProjectedBalance") Boolean projectedBalance) throws GeneralException;

    @RequestLine("POST /finance/charges/disposition-tenant-payment?userId={userId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void dispositionTenantPayment(@Param("token") String token,DispositionWrapper dispositionWrapper, @Param("userId") String userId) throws GeneralException;

}
