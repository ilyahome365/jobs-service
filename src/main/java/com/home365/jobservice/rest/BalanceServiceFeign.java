package com.home365.jobservice.rest;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.enums.TransactionType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.*;
import com.home365.jobservice.model.wrapper.CancelChargeWrapper;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import com.home365.jobservice.model.wrapper.OwnerProjectedBalanceWrapper;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface BalanceServiceFeign {

    @RequestLine("POST /bills/cancel-bills-by-property-and-dueDate")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<Integer> cancelBillsByPropertyAndDueDate(@Param("token") String token,
                                                  PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;

    @RequestLine("POST /charges/cancel-all-recurring")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<String> cancelAllRecurringByAccount(@Param("token") String token,
                                             CancelChargeWrapper cancelChargeWrapper) throws GeneralException;

    @RequestLine("POST /bills/create-owner-bill-for-tenant-debts")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void createOwnerBillForTenantDebts(@Param("token") String token,
                                       OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("POST /bills/create-Termination-Fee")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String createTerminationFeeByProperty(@Param("token") String token,
                                          OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("POST /bills/create-material-transfer-fee")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String createMaterialTransferFee(@Param("token") String token,
                                     OwnerBillsWrapper ownerBillsWrapper) throws GeneralException;

    @RequestLine("GET /move-security-deposit-to-owner-account?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    String moveSecurityDepositToOwnerAccount(@Param("token") String token,
                                             @Param("propertyId") String propertyId) throws GeneralException;

    @RequestLine("GET /get-owner-projected-balance")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    OwnerProjectedBalanceWrapper getAllOwnersProjectedBalance(@Param("token") String token) throws GeneralException;


    @RequestLine("GET /move-late-fee-to-home365?chargesIds={chargesIds}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void moveLateFeeToPM(@Param("token") String token ,@Param("chargesIds") List<String>chargesIds) throws GeneralException;

    @RequestLine("GET /account/{accountId}/balance?isProjectedBalance={isProjectedBalance}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    AccountBalance getOwnerProjectedBalance(@Param("token") String token, @Param("accountId") String accountId, @Param("isProjectedBalance") Boolean projectedBalance) throws GeneralException;

    @RequestLine("POST /charges/disposition-tenant-payment?userId={userId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void dispositionTenantPayment(@Param("token") String token,DispositionWrapper dispositionWrapper, @Param("userId") String userId) throws GeneralException;


    @RequestLine("POST /payment-notification")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void notifyTransactionPaid(@Param("token") String token, PaymentNotification notificationOfPayment) throws GeneralException;



    @RequestLine("GET /create-welcome-credit")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<String> createWelcomeCredit(@Param("token") String token) throws GeneralException;

    @RequestLine("GET /transactions/get-by-charge-account-and-billType?chargeAccount={chargeAccount}&transactionType={transactionType}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<Transactions> getTransactionsByChargeAccountAndBillType(@Param("token") String token,
                                                                 @Param("chargeAccount") String chargeAccount,
                                                                 @Param("transactionType") TransactionType transactionStatus) throws GeneralException;


    @RequestLine("POST /charges/charge-with-stripe")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void chargeWithStripe(@Param("token") String token, ChargeWithStripeRequest chargeWithStripeRequest) throws GeneralException;


}
