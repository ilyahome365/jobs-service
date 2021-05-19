package com.home365.jobservice.utils;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.model.ChargeWithStripeRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class CodeUtils {
    public static ChargeWithStripeRequest createChargeWithStripe(List<Transactions> transactions, List<Transactions> refundTransactions, long feeAmount,
                                                                 Boolean sendMail, String description, Boolean isRefunded) {
        ChargeWithStripeRequest chargeWithStripeRequest = new ChargeWithStripeRequest();
        chargeWithStripeRequest.setCharges(transactions.stream().map(Transactions::getTransactionId).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(refundTransactions))
            chargeWithStripeRequest.setRefundTransactions(refundTransactions.stream().filter(Objects::nonNull).map(Transactions::getTransactionId).collect(Collectors.toList()));
        chargeWithStripeRequest.setFeeAmount(feeAmount);
        chargeWithStripeRequest.setSendMailFlag(sendMail);
        chargeWithStripeRequest.setDescription(description);
        chargeWithStripeRequest.setIsRefunded(isRefunded);
        return chargeWithStripeRequest;
    }
}
