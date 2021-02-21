package com.home365.jobservice.model;

import com.home365.jobservice.entities.AccountExtensionBase;
import com.home365.jobservice.entities.Payments;
import com.home365.jobservice.entities.Transactions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionChargeWrapper {

    private AccountExtensionBase receiveAccountExtensionBase;
    private AccountExtensionBase chargeAccountExtensionBase;

    private List<Transactions> transaction;
    private StripePayment stripePayment;
    private Payments payments;
    private Integer amount;
    private Transactions convenientFeeTransaction;
    private Boolean ownerRefund;

    public boolean isHaveStripePayment() {
        return stripePayment != null;
    }

    public boolean isHavePayment() {
        return payments != null;
    }

    public boolean isHaveAccountsExtensionBase() {
        return receiveAccountExtensionBase != null && chargeAccountExtensionBase != null;
    }

    public double getConvenientFeeAmount() {
        if (convenientFeeTransaction != null) {
            return convenientFeeTransaction.getAmount();
        }
        return 0;
    }
}
