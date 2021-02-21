package com.home365.jobservice.service;

import com.home365.jobservice.model.StripePayment;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

public interface PaymentStripeService {
    Charge createCharge(String stripeCustomerId,
                        int amount,
                        String destinationStripeAccountId,
                        String description,
                        boolean addAchFee) throws StripeException;

    StripePayment createPaymentTypeAndSendForPayment(String receiveStripeAccountId,
                                                     String locationKey,
                                                     String chargeStripeAccountId,
                                                     double amount,
                                                     String accountId,
                                                     String ownerName,
                                                     String description) throws StripeException;
}
